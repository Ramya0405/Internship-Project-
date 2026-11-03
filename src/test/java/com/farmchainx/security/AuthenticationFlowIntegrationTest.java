package com.farmchainx.security;

import com.farmchainx.entity.User;
import com.farmchainx.enums.UserRole;
import com.farmchainx.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationFlowIntegrationTest {

    private static final String TEST_PASSWORD = "StrongTestPass123";

    private static final Map<UserRole, String> ROLE_DESTINATIONS = Map.of(
            UserRole.ADMIN, "/admin/dashboard",
            UserRole.FARMER, "/farmer/dashboard",
            UserRole.DISTRIBUTOR, "/distributor/dashboard",
            UserRole.WAREHOUSE_MANAGER, "/warehouse/dashboard",
            UserRole.RETAILER, "/retailer/dashboard",
            UserRole.CONSUMER, "/consumer/track");

    private static final Map<UserRole, String> ROLE_VIEWS = Map.of(
            UserRole.ADMIN, "admin/dashboard",
            UserRole.FARMER, "farmer/dashboard",
            UserRole.DISTRIBUTOR, "distributor/dashboard",
            UserRole.WAREHOUSE_MANAGER, "warehouse/dashboard",
            UserRole.RETAILER, "retailer/dashboard",
            UserRole.CONSUMER, "consumer/track");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Map<UserRole, String> testEmails = new EnumMap<>(UserRole.class);

    @BeforeEach
    void createUsersForEveryRole() {
        for (UserRole role : UserRole.values()) {
            String email = "login-" + role.name().toLowerCase() + "-"
                    + System.nanoTime() + "@example.test";
            User user = new User();
            user.setFullName("Login Test " + role.name());
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
            user.setRole(role);
            userRepository.saveAndFlush(user);
            testEmails.put(role, email);
        }
    }

    @Test
    void validCredentialsRedirectEveryRoleToItsRenderedDestination() throws Exception {
        for (UserRole role : UserRole.values()) {
            MvcResult login = mockMvc.perform(post("/login")
                            .param("email", testEmails.get(role))
                            .param("password", TEST_PASSWORD)
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(ROLE_DESTINATIONS.get(role)))
                    .andReturn();

            MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
            assertThat(session).as("authenticated session for %s", role).isNotNull();

            mockMvc.perform(get(ROLE_DESTINATIONS.get(role)).session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name(ROLE_VIEWS.get(role)));
        }
    }

    @Test
    void loginRequiresCsrfAndInvalidCredentialsReturnToLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", testEmails.get(UserRole.ADMIN))
                        .param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));

        mockMvc.perform(post("/login")
                        .param("email", testEmails.get(UserRole.ADMIN))
                        .param("password", "incorrect-password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    void registrationValidatesInputAndCreatesOnlyAConsumer() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "Invalid")
                        .param("email", "invalid-email")
                        .param("password", "weak")
                        .param("confirmPassword", "different")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));

        String email = "registration-" + System.nanoTime() + "@example.test";
        mockMvc.perform(post("/register")
                        .param("fullName", "Registered Consumer")
                        .param("email", email)
                        .param("password", TEST_PASSWORD)
                        .param("confirmPassword", TEST_PASSWORD)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        User registered = userRepository.findByEmail(email).orElseThrow();
        assertThat(registered.getRole()).isEqualTo(UserRole.CONSUMER);
        assertThat(passwordEncoder.matches(TEST_PASSWORD, registered.getPassword())).isTrue();
    }
}
