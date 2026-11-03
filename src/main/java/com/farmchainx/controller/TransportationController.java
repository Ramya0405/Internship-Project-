package com.farmchainx.controller;

import com.farmchainx.dto.TransportationDto;
import com.farmchainx.enums.TransportationStatus;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.ProductBatchService;
import com.farmchainx.service.TransportationService;
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
@RequestMapping("/distributor/transportation")
@PreAuthorize("hasRole('DISTRIBUTOR') or hasRole('ADMIN')")
public class TransportationController extends DashboardController {

    private final TransportationService  transportationService;
    private final ProductBatchService    batchService;

    public TransportationController(TransportationService transportationService,
                                    ProductBatchService batchService,
                                    NotificationService notificationService) {
        super(notificationService);
        this.transportationService = transportationService;
        this.batchService          = batchService;
    }

    @GetMapping({"", "/list"})
    public String list(@RequestParam(defaultValue = "0")  int page,
                       @RequestParam(defaultValue = "10") int size,
                       @AuthenticationPrincipal CustomUserDetails principal,
                       Model model) {
        addCommonAttributes(model, principal);
        var transports = transportationService.findByDistributor(
                principal.getUser(),
                PageRequest.of(page, size, Sort.by("departureDate").descending()));
        model.addAttribute("transports",  transports);
        model.addAttribute("currentPage", page);
        return "distributor/transportation-list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long batchId,
                             @AuthenticationPrincipal CustomUserDetails principal,
                             Model model) {
        addCommonAttributes(model, principal);
        TransportationDto dto = new TransportationDto();
        if (batchId != null) dto.setBatchId(batchId);
        model.addAttribute("transportationDto", dto);
        model.addAttribute("statusValues",      TransportationStatus.values());
        model.addAttribute("editMode",          false);
        return "distributor/transportation-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("transportationDto") TransportationDto dto,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, principal);
            model.addAttribute("statusValues", TransportationStatus.values());
            model.addAttribute("editMode",     false);
            return "distributor/transportation-form";
        }
        try {
            transportationService.create(dto, principal.getUser());
            redirectAttributes.addFlashAttribute("successMsg", "Transportation record created.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/distributor/dashboard";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails principal,
                           Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("transportationDto", transportationService.findById(id, principal.getUser()));
        model.addAttribute("statusValues",      TransportationStatus.values());
        model.addAttribute("editMode",          true);
        return "distributor/transportation-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("transportationDto") TransportationDto dto,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirectAttributes) {
        try {
            transportationService.update(id, dto, principal.getUser());
            redirectAttributes.addFlashAttribute("successMsg", "Transportation updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/distributor/dashboard";
    }
}
