package com.farmchainx.dto;

import com.farmchainx.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public class AdminUserRequest extends RegistrationRequest {

    @NotNull(message = "A role is required")
    private UserRole role;

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
