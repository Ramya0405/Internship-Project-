package com.farmchainx.controller;

import com.farmchainx.dto.QualityInspectionDto;
import com.farmchainx.enums.QualityGrade;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.ProductBatchService;
import com.farmchainx.service.QualityInspectionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/quality")
@PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER','ADMIN')")
public class QualityInspectionController extends DashboardController {

    private final QualityInspectionService inspectionService;
    private final ProductBatchService      batchService;

    public QualityInspectionController(QualityInspectionService inspectionService,
                                       ProductBatchService batchService,
                                       NotificationService notificationService) {
        super(notificationService);
        this.inspectionService = inspectionService;
        this.batchService      = batchService;
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long batchId,
                             @AuthenticationPrincipal CustomUserDetails principal,
                             Model model) {
        addCommonAttributes(model, principal);
        QualityInspectionDto dto = new QualityInspectionDto();
        dto.setInspectionDate(LocalDate.now());
        if (batchId != null) dto.setBatchId(batchId);
        model.addAttribute("inspectionDto", dto);
        model.addAttribute("grades",        QualityGrade.values());
        return "quality/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("inspectionDto") QualityInspectionDto dto,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, principal);
            model.addAttribute("grades", QualityGrade.values());
            return "quality/form";
        }
        try {
            inspectionService.create(dto, principal.getUser());
            redirectAttributes.addFlashAttribute("successMsg", "Quality inspection recorded.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/warehouse/dashboard";
    }

    @GetMapping("/batch/{batchId}")
    public String listForBatch(@PathVariable Long batchId,
                               @AuthenticationPrincipal CustomUserDetails principal,
                               Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("inspections", inspectionService.findByBatch(batchId, principal.getUser()));
        model.addAttribute("batch",       batchService.findById(batchId, principal.getUser()));
        return "quality/list";
    }
}
