package com.farmchainx.controller;

import com.farmchainx.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardHomeController {

    @GetMapping("/dashboard-home")
    public String home(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        return switch (principal.getRole()) {
            case "ADMIN" -> "redirect:/admin/dashboard";
            case "FARMER" -> "redirect:/farmer/dashboard";
            case "DISTRIBUTOR" -> "redirect:/distributor/dashboard";
            case "WAREHOUSE_MANAGER" -> "redirect:/warehouse/dashboard";
            case "RETAILER" -> "redirect:/retailer/dashboard";
            default -> "redirect:/consumer/track";
        };
    }
}
