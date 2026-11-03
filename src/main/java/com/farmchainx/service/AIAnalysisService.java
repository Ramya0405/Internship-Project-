package com.farmchainx.service;

import com.farmchainx.dto.AIAnalysisDto;
import com.farmchainx.enums.RiskLevel;
import com.farmchainx.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AIAnalysisService {

    /** Run risk analysis on a batch and persist the result. */
    AIAnalysisDto analyze(Long batchId);

    AIAnalysisDto analyze(Long batchId, User requestingUser);

    /** Most recent analysis for a batch. */
    AIAnalysisDto getLatestForBatch(Long batchId);

    AIAnalysisDto getLatestForBatch(Long batchId, User requestingUser);

    /** All analyses for a batch (most recent first). */
    List<AIAnalysisDto> getHistoryForBatch(Long batchId);

    List<AIAnalysisDto> getHistoryForBatch(Long batchId, User requestingUser);

    /** Paginated list filtered by risk level – used for the alert dashboard. */
    Page<AIAnalysisDto> getAlerts(RiskLevel riskLevel, Pageable pageable);

    /** All HIGH risk alerts (most recent first). */
    List<AIAnalysisDto> getHighRiskAlerts();

    long countByRiskLevel(RiskLevel riskLevel);
}
