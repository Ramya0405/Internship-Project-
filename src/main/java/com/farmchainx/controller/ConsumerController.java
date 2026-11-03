package com.farmchainx.controller;

import com.farmchainx.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Consumer-facing pages: product tracking lookup (public and authenticated).
 */
@Controller
public class ConsumerController extends DashboardController {

    public ConsumerController(NotificationService notificationService) {
        super(notificationService);
    }

    @GetMapping({"/consumer/track", "/track"})
    public String trackPage(@RequestParam(value = "id", required = false) String trackingId,
                            Model model) {
        model.addAttribute("trackingId", trackingId);
        return "consumer/track";
    }
}
