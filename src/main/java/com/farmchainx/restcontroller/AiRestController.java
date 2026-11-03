package com.farmchainx.restcontroller;

import com.farmchainx.dto.AIAnalysisDto;
import com.farmchainx.enums.RiskLevel;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.AIAnalysisService;
import com.farmchainx.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the AI-assisted risk analysis module.
 */
@RestController
@RequestMapping("/api/ai")
public class AiRestController {

    private final AIAnalysisService aiAnalysisService;

    public AiRestController(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    /** Trigger a fresh risk analysis on a batch. */
    @PostMapping("/analyze/{batchId}")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<AIAnalysisDto>> analyze(
            @PathVariable Long batchId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        AIAnalysisDto result = aiAnalysisService.analyze(batchId, principal.getUser());
        return ResponseEntity.ok(ApiResponse.success("Risk analysis completed", result));
    }

    /** Retrieve the most recent analysis result for a batch. */
    @GetMapping("/risk/{batchId}")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER','DISTRIBUTOR','WAREHOUSE_MANAGER','RETAILER')")
    public ResponseEntity<ApiResponse<AIAnalysisDto>> getRisk(
            @PathVariable Long batchId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                aiAnalysisService.getLatestForBatch(batchId, principal.getUser())));
    }

    /** Full analysis history for a batch (most recent first). */
    @GetMapping("/history/{batchId}")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER','DISTRIBUTOR','WAREHOUSE_MANAGER','RETAILER')")
    public ResponseEntity<ApiResponse<List<AIAnalysisDto>>> getHistory(
            @PathVariable Long batchId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                aiAnalysisService.getHistoryForBatch(batchId, principal.getUser())));
    }

    /** All HIGH and MEDIUM risk alerts for the dashboard. */
    @GetMapping("/alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AIAnalysisDto>>> getAlerts(
            @RequestParam(defaultValue = "HIGH") RiskLevel level,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size) {
        Page<AIAnalysisDto> alerts = aiAnalysisService.getAlerts(
                level, PageRequest.of(page, size, Sort.by("analyzedAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    /** Summary counts per risk level. */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> summary() {
        var summary = new java.util.LinkedHashMap<String, Long>();
        summary.put("high",   aiAnalysisService.countByRiskLevel(RiskLevel.HIGH));
        summary.put("medium", aiAnalysisService.countByRiskLevel(RiskLevel.MEDIUM));
        summary.put("low",    aiAnalysisService.countByRiskLevel(RiskLevel.LOW));
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
