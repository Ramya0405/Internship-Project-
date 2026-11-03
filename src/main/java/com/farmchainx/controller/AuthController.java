package com.farmchainx.controller;

import com.farmchainx.dto.RegistrationRequest;
import com.farmchainx.exception.DuplicateEmailException;
import com.farmchainx.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles login, registration, and logout pages (Thymeleaf).
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ── Root redirect ─────────────────────────────────────────────────────────

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    // ── Dashboard redirect (role-based) ───────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return switch (role) {
            case "ROLE_ADMIN"             -> "redirect:/admin/dashboard";
            case "ROLE_FARMER"            -> "redirect:/farmer/dashboard";
            case "ROLE_DISTRIBUTOR"       -> "redirect:/distributor/dashboard";
            case "ROLE_WAREHOUSE_MANAGER" -> "redirect:/warehouse/dashboard";
            case "ROLE_RETAILER"          -> "redirect:/retailer/dashboard";
            default                       -> "redirect:/consumer/track";
        };
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error",   required = false) String error,
            @RequestParam(value = "logout",  required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            Authentication authentication,
            Model model) {

        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        if (error   != null) model.addAttribute("errorMsg",   "Invalid email or password.");
        if (logout  != null) model.addAttribute("logoutMsg",  "You have been logged out.");
        if (expired != null) model.addAttribute("expiredMsg", "Your session has expired. Please log in again.");
        return "auth/login";
    }

    // ── Register ─────────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String registerPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registrationRequest") RegistrationRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Extra cross-field validation: password match
        if (!request.passwordsMatch()) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(request);
            redirectAttributes.addFlashAttribute("successMsg",
                "Account created successfully! Please log in.");
            return "redirect:/login";
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            return "auth/register";
        }
    }

    // ── Access Denied ─────────────────────────────────────────────────────────

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
