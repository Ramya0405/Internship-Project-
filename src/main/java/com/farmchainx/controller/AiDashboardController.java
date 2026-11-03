package com.farmchainx.controller;

import com.farmchainx.enums.RiskLevel;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.AIAnalysisService;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.ProductBatchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ai")
@PreAuthorize("hasAnyRole('ADMIN','FARMER','WAREHOUSE_MANAGER')")
public class AiDashboardController extends DashboardController {

    private final AIAnalysisService   aiAnalysisService;
    private final ProductBatchService batchService;

    public AiDashboardController(AIAnalysisService aiAnalysisService,
                                 ProductBatchService batchService,
                                 NotificationService notificationService) {
        super(notificationService);
        this.aiAnalysisService = aiAnalysisService;
        this.batchService      = batchService;
    }

    /** AI risk alert dashboard – shows all HIGH risk batches. */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        addCommonAttributes(model, principal);

        model.addAttribute("highRiskAlerts",  aiAnalysisService.getHighRiskAlerts());
        model.addAttribute("highCount",       aiAnalysisService.countByRiskLevel(RiskLevel.HIGH));
        model.addAttribute("mediumCount",     aiAnalysisService.countByRiskLevel(RiskLevel.MEDIUM));
        model.addAttribute("lowCount",        aiAnalysisService.countByRiskLevel(RiskLevel.LOW));

        return "ai/dashboard";
    }

    /** Run analysis on a specific batch, then show results. */
    @PostMapping("/analyze/{batchId}")
    public String analyze(@PathVariable Long batchId,
                          @AuthenticationPrincipal CustomUserDetails principal,
                          RedirectAttributes redirectAttributes) {
        try {
            var result = aiAnalysisService.analyze(batchId, principal.getUser());
            redirectAttributes.addFlashAttribute("analysisResult", result);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Risk analysis complete. Score: " + result.getRiskScore() + "/100 (" + result.getRiskLevel() + ")");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/ai/batch/" + batchId;
    }

    /** Show all analysis results for a batch. */
    @GetMapping("/batch/{batchId}")
    public String batchAnalysis(@PathVariable Long batchId,
                                @AuthenticationPrincipal CustomUserDetails principal,
                                Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("batch",    batchService.findById(batchId, principal.getUser()));
        model.addAttribute("analyses", aiAnalysisService.getHistoryForBatch(batchId, principal.getUser()));
        return "ai/batch-analysis";
    }
}
