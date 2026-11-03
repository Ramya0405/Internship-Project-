package com.farmchainx.controller;

import com.farmchainx.enums.BatchStatus;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.FarmService;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.ProductBatchService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/farmer")
@PreAuthorize("hasRole('FARMER')")
public class FarmerDashboardController extends DashboardController {

    private final FarmService         farmService;
    private final ProductBatchService batchService;

    public FarmerDashboardController(FarmService farmService,
                                     ProductBatchService batchService,
                                     NotificationService notificationService) {
        super(notificationService);
        this.farmService  = farmService;
        this.batchService = batchService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        addCommonAttributes(model, principal);

        model.addAttribute("farmCount",       farmService.countByOwner(principal.getUser()));
        model.addAttribute("batchCount",      batchService.countByFarmer(principal.getUser()));
        model.addAttribute("inTransitCount",  batchService.countByStatus(BatchStatus.IN_TRANSIT));
        model.addAttribute("soldCount",       batchService.countByStatus(BatchStatus.SOLD));

        return "farmer/dashboard";
    }
}
