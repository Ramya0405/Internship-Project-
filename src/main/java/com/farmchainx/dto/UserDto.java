package com.farmchainx.dto;

import com.farmchainx.entity.User;
import com.farmchainx.enums.UserRole;

import java.time.LocalDateTime;

/**
 * Read-only DTO representing a user. Never exposes the password hash.
 */
public class UserDto {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private LocalDateTime createdAt;

    public UserDto() {}

    /** Factory method – converts an entity to this DTO. */
    public static UserDto from(User user) {
        UserDto dto = new UserDto();
        dto.id          = user.getId();
        dto.fullName    = user.getFullName();
        dto.email       = user.getEmail();
        dto.phoneNumber = user.getPhoneNumber();
        dto.role        = user.getRole();
        dto.createdAt   = user.getCreatedAt();
        return dto;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
