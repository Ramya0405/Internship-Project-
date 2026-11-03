package com.farmchainx.service.impl;

import com.farmchainx.dto.RegistrationRequest;
import com.farmchainx.dto.UserDto;
import com.farmchainx.entity.User;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.DuplicateEmailException;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.repository.UserRepository;
import com.farmchainx.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Registration ─────────────────────────────────────────────────────────

    @Override
    public UserDto register(RegistrationRequest request) {
        return createUserAccount(request, UserRole.CONSUMER);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto createUser(RegistrationRequest request, UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("A role is required");
        }
        return createUserAccount(request, role);
    }

    private UserDto createUserAccount(RegistrationRequest request, UserRole role) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        if (!request.passwordsMatch()) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(role);

        return UserDto.from(userRepository.save(user));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return UserDto.from(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findByEmail(String email) {
        return UserDto.from(getEntityByEmail(email));
    }

    @Override
    @Transactional(readOnly = true)
    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findByRole(UserRole role) {
        return userRepository.findByRole(role)
                .stream()
                .map(UserDto::from)
                .collect(Collectors.toList());
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public UserDto update(Long id, UserDto dto) {
        User user = getEntityById(id);

        if (dto.getFullName() != null)    user.setFullName(dto.getFullName());
        if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getRole() != null)        user.setRole(dto.getRole());

        // Email change: check uniqueness
        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateEmailException(dto.getEmail());
            }
            user.setEmail(dto.getEmail().toLowerCase().trim());
        }

        return UserDto.from(userRepository.save(user));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(UserRole role) {
        return userRepository.countByRole(role);
    }
}
