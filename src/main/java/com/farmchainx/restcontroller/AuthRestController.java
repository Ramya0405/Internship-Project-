package com.farmchainx.restcontroller;

import com.farmchainx.dto.RegistrationRequest;
import com.farmchainx.dto.UserDto;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.UserService;
import com.farmchainx.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for authentication.
 * Login and logout are handled by Spring Security's form-login mechanism.
 * These endpoints expose register and current-user info for API clients.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserService userService;

    public AuthRestController(UserService userService) {
        this.userService = userService;
    }

    /** Register a new account. */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody RegistrationRequest request) {
        UserDto created = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", created));
    }

    /** Returns the currently authenticated user. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> currentUser(
            @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.success(UserDto.from(principal.getUser())));
    }

    /** Invalidates the current session (programmatic logout for API clients). */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
