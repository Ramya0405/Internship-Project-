package com.farmchainx.dto;

import jakarta.validation.constraints.*;

/**
 * Request payload for new user registration.
 */
public class RegistrationRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be 2–100 characters")
    private String fullName;

    @Email(message = "A valid email address is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /** Convenience method used by the service layer. */
    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
