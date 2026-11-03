package com.farmchainx.controller;

import com.farmchainx.enums.BatchStatus;
import com.farmchainx.enums.UserRole;
import com.farmchainx.dto.AdminUserRequest;
import com.farmchainx.exception.DuplicateEmailException;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController extends DashboardController {

    private final UserService         userService;
    private final FarmService         farmService;
    private final ProductBatchService batchService;

    public AdminDashboardController(UserService userService,
                                    FarmService farmService,
                                    ProductBatchService batchService,
                                    NotificationService notificationService) {
        super(notificationService);
        this.userService  = userService;
        this.farmService  = farmService;
        this.batchService = batchService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        addCommonAttributes(model, principal);

        model.addAttribute("totalUsers",      userService.findAll(PageRequest.of(0,1)).getTotalElements());
        model.addAttribute("totalFarms",      farmService.findAll(PageRequest.of(0,1)).getTotalElements());
        model.addAttribute("totalBatches",    batchService.findAll(PageRequest.of(0,1)).getTotalElements());
        model.addAttribute("inTransitCount",  batchService.countByStatus(BatchStatus.IN_TRANSIT));
        model.addAttribute("recalledCount",   batchService.countByStatus(BatchStatus.RECALLED));
        model.addAttribute("farmerCount",     userService.countByRole(UserRole.FARMER));
        model.addAttribute("distributorCount",userService.countByRole(UserRole.DISTRIBUTOR));

        // Recent batches
        model.addAttribute("recentBatches",
                batchService.findAll(PageRequest.of(0, 8, Sort.by("createdAt").descending())));

        return "admin/dashboard";
    }

    // ── User management ───────────────────────────────────────────────────────

    @GetMapping("/users")
    public String users(@RequestParam(defaultValue = "0")  int page,
                        @RequestParam(defaultValue = "15") int size,
                        @AuthenticationPrincipal CustomUserDetails principal,
                        Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("users",       userService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())));
        model.addAttribute("currentPage", page);
        model.addAttribute("roles",       UserRole.values());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String createUserForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("adminUserRequest", new AdminUserRequest());
        model.addAttribute("roles", UserRole.values());
        return "admin/user-form";
    }

    @PostMapping("/users/new")
    public String createUser(@Valid @ModelAttribute("adminUserRequest") AdminUserRequest request,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal CustomUserDetails principal,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (!request.passwordsMatch()) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, principal);
            model.addAttribute("roles", UserRole.values());
            return "admin/user-form";
        }

        try {
            userService.createUser(request, request.getRole());
            redirectAttributes.addFlashAttribute("successMsg", "User created successfully.");
            return "redirect:/admin/users";
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            addCommonAttributes(model, principal);
            model.addAttribute("roles", UserRole.values());
            return "admin/user-form";
        }
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails principal,
                             org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        if (id.equals(principal.getUserId())) {
            ra.addFlashAttribute("errorMsg", "You cannot delete your own account.");
        } else {
            userService.deleteById(id);
            ra.addFlashAttribute("successMsg", "User deleted.");
        }
        return "redirect:/admin/users";
    }

    // ── All batches ───────────────────────────────────────────────────────────

    @GetMapping("/batches")
    public String batches(@RequestParam(defaultValue = "0")  int page,
                          @RequestParam(defaultValue = "15") int size,
                          @AuthenticationPrincipal CustomUserDetails principal,
                          Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("batches",      batchService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())));
        model.addAttribute("currentPage",  page);
        model.addAttribute("statusValues", BatchStatus.values());
        return "admin/batches";
    }

    // ── All farms ─────────────────────────────────────────────────────────────

    @GetMapping("/farms")
    public String farms(@RequestParam(defaultValue = "0")  int page,
                        @RequestParam(defaultValue = "15") int size,
                        @AuthenticationPrincipal CustomUserDetails principal,
                        Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("farms",       farmService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())));
        model.addAttribute("currentPage", page);
        return "admin/farms";
    }
}
