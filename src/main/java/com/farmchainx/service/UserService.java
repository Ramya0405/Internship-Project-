package com.farmchainx.service;

import com.farmchainx.dto.RegistrationRequest;
import com.farmchainx.dto.UserDto;
import com.farmchainx.entity.User;
import com.farmchainx.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Business operations for user management and authentication.
 */
public interface UserService {

    /** Registers a new user account. Throws {@link com.farmchainx.exception.DuplicateEmailException} if email taken. */
    UserDto register(RegistrationRequest request);

    UserDto createUser(RegistrationRequest request, UserRole role);

    UserDto findById(Long id);

    UserDto findByEmail(String email);

    User getEntityById(Long id);

    User getEntityByEmail(String email);

    Page<UserDto> findAll(Pageable pageable);

    List<UserDto> findByRole(UserRole role);

    UserDto update(Long id, UserDto dto);

    void deleteById(Long id);

    boolean existsByEmail(String email);

    /** Dashboard statistics */
    long countByRole(UserRole role);
}
