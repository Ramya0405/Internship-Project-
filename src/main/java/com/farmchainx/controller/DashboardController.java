package com.farmchainx.controller;

import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.NotificationService;
import org.springframework.ui.Model;

/**
 * Shared dashboard helper – adds common model attributes used across all role dashboards.
 * Role-specific controllers extend this or call addCommonAttributes() directly.
 */
public class DashboardController {

    protected final NotificationService notificationService;

    public DashboardController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** Adds unread notification count and current user to the model. */
    protected void addCommonAttributes(Model model, CustomUserDetails principal) {
        if (principal != null) {
            model.addAttribute("currentUser",      principal.getUser());
            model.addAttribute("unreadCount",
                notificationService.countUnreadForUser(principal.getUser()));
        }
    }

}
