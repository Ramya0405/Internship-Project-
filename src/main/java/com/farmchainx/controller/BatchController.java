package com.farmchainx.controller;

import com.farmchainx.dto.ProductBatchDto;
import com.farmchainx.dto.FarmDto;
import com.farmchainx.entity.User;
import com.farmchainx.enums.BatchStatus;
import com.farmchainx.enums.UserRole;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.FarmService;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.ProductBatchService;
import com.farmchainx.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
@RequestMapping("/farmer/batches")
@PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
public class BatchController extends DashboardController {

    private final ProductBatchService batchService;
    private final FarmService         farmService;
    private final ProductService      productService;

    public BatchController(ProductBatchService batchService,
                           FarmService farmService,
                           ProductService productService,
                           NotificationService notificationService) {
        super(notificationService);
        this.batchService   = batchService;
        this.farmService    = farmService;
        this.productService = productService;
    }

    // ── List ──────────────────────────────────────────────────────────────────

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails principal,
                       @RequestParam(defaultValue = "0")  int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        addCommonAttributes(model, principal);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductBatchDto> batches = "ADMIN".equals(principal.getRole())
                ? batchService.findAll(pageable)
                : batchService.findByFarmer(principal.getUser(), pageable);
        model.addAttribute("batches",      batches);
        model.addAttribute("currentPage",  page);
        model.addAttribute("statusValues", BatchStatus.values());
        return "farmer/batches";
    }

    // ── Create form ───────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("batchDto",  new ProductBatchDto());
        model.addAttribute("farms",     farmsFor(principal.getUser()));
        model.addAttribute("products",  productService.findAll(PageRequest.of(0, 200)).getContent());
        model.addAttribute("editMode",  false);
        return "farmer/batch-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("batchDto") ProductBatchDto dto,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, principal);
            model.addAttribute("farms",    farmsFor(principal.getUser()));
            model.addAttribute("products", productService.findAll(PageRequest.of(0, 200)).getContent());
            model.addAttribute("editMode", false);
            return "farmer/batch-form";
        }
        ProductBatchDto created = batchService.create(dto, principal.getUser());
        redirectAttributes.addFlashAttribute("successMsg",
                "Batch created. Tracking ID: " + created.getTrackingId());
        return "redirect:/farmer/batches";
    }

    private java.util.List<FarmDto> farmsFor(User user) {
        if (UserRole.ADMIN.equals(user.getRole())) {
            return farmService.findAll(PageRequest.of(0, 200)).getContent();
        }
        return farmService.findByOwner(user);
    }

    // ── Detail / tracking view ────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         Model model) {
        addCommonAttributes(model, principal);
        ProductBatchDto batch = batchService.findById(id, principal.getUser());
        model.addAttribute("batch", batch);

        java.util.List<BatchStatus> allowedStatuses;
        if ("ADMIN".equals(principal.getRole())) {
            allowedStatuses = java.util.List.of(BatchStatus.values());
        } else if ("FARMER".equals(principal.getRole())) {
            allowedStatuses = batch.getCurrentStatus() == BatchStatus.CREATED
                    ? java.util.List.of(BatchStatus.IN_TRANSIT)
                    : java.util.List.of();
        } else {
            allowedStatuses = java.util.List.of(BatchStatus.values());
        }
        model.addAttribute("statusValues", allowedStatuses);
        return "farmer/batch-detail";
    }

    // ── Status update ─────────────────────────────────────────────────────────

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam BatchStatus status,
                               @AuthenticationPrincipal CustomUserDetails principal,
                               RedirectAttributes redirectAttributes) {
        try {
            batchService.updateStatus(id, status, principal.getUser());
            redirectAttributes.addFlashAttribute("successMsg", "Batch status updated to " + status);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/farmer/batches/" + id;
    }
}
