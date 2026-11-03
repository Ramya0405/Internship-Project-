package com.farmchainx.controller;

import com.farmchainx.entity.Notification;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController extends DashboardController {

    public NotificationController(NotificationService notificationService) {
        super(notificationService);
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0")  int page,
                       @RequestParam(defaultValue = "15") int size,
                       @AuthenticationPrincipal CustomUserDetails principal,
                       Model model) {
        addCommonAttributes(model, principal);
        Page<Notification> notifications = notificationService.getAllForUser(
                principal.getUser(),
                PageRequest.of(page, size));
        model.addAttribute("notifications", notifications);
        model.addAttribute("currentPage",   page);
        return "notifications/list";
    }

    @PostMapping("/read-all")
    public String markAllRead(@AuthenticationPrincipal CustomUserDetails principal,
                             RedirectAttributes redirectAttributes) {
        int updated = notificationService.markAllReadForUser(principal.getUser());
        if (updated > 0) {
            redirectAttributes.addFlashAttribute("successMsg", "All notifications marked as read.");
        }
        return "redirect:/notifications";
    }
}
