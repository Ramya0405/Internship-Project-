package com.farmchainx.controller;

import com.farmchainx.enums.BatchStatus;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.ProductBatchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/distributor")
@PreAuthorize("hasRole('DISTRIBUTOR')")
public class DistributorController extends DashboardController {

    private final ProductBatchService batchService;

    public DistributorController(ProductBatchService batchService,
                                 NotificationService notificationService) {
        super(notificationService);
        this.batchService = batchService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("inTransitBatches",
                batchService.findByStatus(BatchStatus.IN_TRANSIT,
                        PageRequest.of(0, 8, Sort.by("createdAt").descending())));
        model.addAttribute("inTransitCount", batchService.countByStatus(BatchStatus.IN_TRANSIT));
        return "distributor/dashboard";
    }

    @GetMapping("/batches")
    public String batches(@RequestParam(defaultValue = "0")  int page,
                          @RequestParam(defaultValue = "10") int size,
                          @AuthenticationPrincipal CustomUserDetails principal,
                          Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("batches",
                batchService.findByStatus(BatchStatus.IN_TRANSIT,
                        PageRequest.of(page, size, Sort.by("createdAt").descending())));
        model.addAttribute("currentPage", page);
        return "distributor/batches";
    }
}
