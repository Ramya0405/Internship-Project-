package com.farmchainx.service;

import com.farmchainx.TestEntityHelper;
import com.farmchainx.dto.RegistrationRequest;
import com.farmchainx.dto.UserDto;
import com.farmchainx.entity.User;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.DuplicateEmailException;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.repository.UserRepository;
import com.farmchainx.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private RegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        TestEntityHelper.withId(sampleUser, 1L);
        sampleUser.setFullName("Ramesh Kumar");
        sampleUser.setEmail("ramesh@farm.com");
        sampleUser.setPassword("encodedPassword123");
        sampleUser.setRole(UserRole.FARMER);
        sampleUser.setPhoneNumber("+919876543210");

        registrationRequest = new RegistrationRequest();
        registrationRequest.setFullName("Ramesh Kumar");
        registrationRequest.setEmail("ramesh@farm.com");
        registrationRequest.setPassword("Secret@123");
        registrationRequest.setConfirmPassword("Secret@123");
        registrationRequest.setPhoneNumber("+919876543210");
    }

    @Test
    @DisplayName("Public registration creates a consumer account")
    void register_success() {
        when(userRepository.existsByEmail("ramesh@farm.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            TestEntityHelper.withId(u, 1L);
            return u;
        });

        UserDto result = userService.register(registrationRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("ramesh@farm.com");
        assertThat(result.getFullName()).isEqualTo("Ramesh Kumar");
        assertThat(result.getRole()).isEqualTo(UserRole.CONSUMER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Updating a user without a role change preserves their current role")
    void update_withoutRoleChange_preservesExistingRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserDto update = new UserDto();
        update.setFullName("Updated Farmer");

        UserDto result = userService.update(1L, update);

        assertThat(result.getRole()).isEqualTo(UserRole.FARMER);
        verify(userRepository).save(argThat(user -> user.getRole() == UserRole.FARMER));
    }

    @Test
    @DisplayName("Registration throws DuplicateEmailException when email exists")
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail("ramesh@farm.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registrationRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("ramesh@farm.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Find user by ID successfully")
    void findById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserDto result = userService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("ramesh@farm.com");
    }

    @Test
    @DisplayName("Find user by ID throws ResourceNotFoundException when absent")
    void findById_notFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Find user by email successfully")
    void findByEmail_found() {
        when(userRepository.findByEmail("ramesh@farm.com")).thenReturn(Optional.of(sampleUser));

        UserDto result = userService.findByEmail("ramesh@farm.com");

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Ramesh Kumar");
    }

    @Test
    @DisplayName("Delete user by ID")
    void deleteById_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteById(1L);

        verify(userRepository).deleteById(1L);
    }
}
