package com.farmchainx.controller;

import com.farmchainx.dto.WarehouseDto;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.WarehouseService;
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
@RequestMapping("/warehouse/manage")
@PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
public class WarehouseViewController extends DashboardController {

    private final WarehouseService warehouseService;

    public WarehouseViewController(WarehouseService warehouseService,
                                   NotificationService notificationService) {
        super(notificationService);
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0")  int page,
                       @RequestParam(defaultValue = "10") int size,
                       @AuthenticationPrincipal CustomUserDetails principal,
                       Model model) {
        addCommonAttributes(model, principal);
        Page<WarehouseDto> warehouses = warehouseService.findAll(
                PageRequest.of(page, size, Sort.by("warehouseName")), principal.getUser());
        model.addAttribute("warehouses",   warehouses);
        model.addAttribute("currentPage",  page);
        return "warehouse/list";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("warehouseDto", new WarehouseDto());
        model.addAttribute("editMode",     false);
        return "warehouse/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("warehouseDto") WarehouseDto dto,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, principal);
            model.addAttribute("editMode", false);
            return "warehouse/form";
        }
        warehouseService.create(dto, principal.getUser());
        redirectAttributes.addFlashAttribute("successMsg", "Storage warehouse registered successfully.");
        return "redirect:/warehouse/manage";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails principal,
                           Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("warehouseDto", warehouseService.findById(id, principal.getUser()));
        model.addAttribute("editMode",     true);
        return "warehouse/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("warehouseDto") WarehouseDto dto,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, principal);
            model.addAttribute("editMode", true);
            return "warehouse/form";
        }
        warehouseService.update(id, dto, principal.getUser());
        redirectAttributes.addFlashAttribute("successMsg", "Warehouse facility updated.");
        return "redirect:/warehouse/manage";
    }
}
