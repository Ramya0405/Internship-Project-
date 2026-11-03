package com.farmchainx.config;

import com.farmchainx.entity.User;
import com.farmchainx.enums.UserRole;
import com.farmchainx.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataInitializerTest {

    private static final String CONFIGURED_EMAIL = "configured-admin@example.test";

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AppProperties appProperties;
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        appProperties = new AppProperties();
        appProperties.getAdmin().setEmail(CONFIGURED_EMAIL);
        appProperties.getAdmin().setPassword("TestOnlyPass123");
        appProperties.getAdmin().setFullName("System Administrator");
        dataInitializer = new DataInitializer();
    }

    @Test
    void createsConfiguredAccountWithAdminRoleWhenNoAccountExists() throws Exception {
        when(userRepository.findByEmail(CONFIGURED_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode("TestOnlyPass123")).thenReturn("encoded-test-password");

        CommandLineRunner runner = dataInitializer.initAdminUser(
                userRepository, passwordEncoder, appProperties);
        runner.run();

        verify(userRepository).save(any(User.class));
        org.mockito.ArgumentCaptor<User> captured =
                org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captured.capture());
        assertThat(captured.getValue().getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(captured.getValue().getPassword()).isEqualTo("encoded-test-password");
    }

    @Test
    void rejectsConfiguredAccountWithNonAdminRoleInsteadOfSilentlySkipping() {
        User existing = new User();
        existing.setRole(UserRole.CONSUMER);
        when(userRepository.findByEmail(CONFIGURED_EMAIL)).thenReturn(Optional.of(existing));

        CommandLineRunner runner = dataInitializer.initAdminUser(
                userRepository, passwordEncoder, appProperties);

        assertThatThrownBy(runner::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not have the ADMIN role");
        verify(userRepository, never()).save(any(User.class));
    }
}
