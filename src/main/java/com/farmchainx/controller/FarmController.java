package com.farmchainx.controller;

import com.farmchainx.dto.FarmDto;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.FarmService;
import com.farmchainx.service.NotificationService;
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
@RequestMapping("/farmer/farms")
@PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
public class FarmController extends DashboardController {

    private final FarmService farmService;

    public FarmController(FarmService farmService,
                          NotificationService notificationService) {
        super(notificationService);
        this.farmService = farmService;
    }

    // ── List ──────────────────────────────────────────────────────────────────

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails principal,
                       @RequestParam(defaultValue = "0")  int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        addCommonAttributes(model, principal);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FarmDto> farms = "ADMIN".equals(principal.getRole())
                ? farmService.findAll(pageable)
                : farmService.findByOwner(principal.getUser(), pageable);
        model.addAttribute("farms",       farms);
        model.addAttribute("currentPage", page);
        return "farmer/farms";
    }

    // ── Create form ───────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("farmDto", new FarmDto());
        model.addAttribute("editMode", false);
        return "farmer/farm-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("farmDto") FarmDto dto,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, principal);
            model.addAttribute("editMode", false);
            return "farmer/farm-form";
        }
        farmService.create(dto, principal.getUser());
        redirectAttributes.addFlashAttribute("successMsg", "Farm registered successfully.");
        return "redirect:/farmer/farms";
    }

    // ── Edit form ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails principal,
                           Model model) {
        FarmDto dto = farmService.findById(id, principal.getUser());
        addCommonAttributes(model, principal);
        model.addAttribute("farmDto",  dto);
        model.addAttribute("editMode", true);
        return "farmer/farm-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("farmDto") FarmDto dto,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, principal);
            model.addAttribute("editMode", true);
            return "farmer/farm-form";
        }
        try {
            farmService.update(id, dto, principal.getUser());
            redirectAttributes.addFlashAttribute("successMsg", "Farm updated successfully.");
        } catch (UnauthorizedAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/farmer/farms";
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirectAttributes) {
        try {
            farmService.delete(id, principal.getUser());
            redirectAttributes.addFlashAttribute("successMsg", "Farm deleted.");
        } catch (UnauthorizedAccessException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/farmer/farms";
    }
}
