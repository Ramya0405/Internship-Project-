package com.farmchainx.service.impl;

import com.farmchainx.ai.AgriculturalRiskAnalyzer;
import com.farmchainx.ai.RiskAnalysisResult;
import com.farmchainx.dto.AIAnalysisDto;
import com.farmchainx.entity.AIAnalysis;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.User;
import com.farmchainx.enums.NotificationType;
import com.farmchainx.enums.RiskLevel;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.repository.AIAnalysisRepository;
import com.farmchainx.service.AIAnalysisService;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.ProductBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AIAnalysisServiceImpl.class);

    private final AIAnalysisRepository    analysisRepository;
    private final ProductBatchService     batchService;
    private final AgriculturalRiskAnalyzer riskAnalyzer;
    private final NotificationService     notificationService;

    public AIAnalysisServiceImpl(AIAnalysisRepository analysisRepository,
                                 ProductBatchService batchService,
                                 AgriculturalRiskAnalyzer riskAnalyzer,
                                 NotificationService notificationService) {
        this.analysisRepository  = analysisRepository;
        this.batchService        = batchService;
        this.riskAnalyzer        = riskAnalyzer;
        this.notificationService = notificationService;
    }

    @Override
    public AIAnalysisDto analyze(Long batchId) {
        ProductBatch batch = batchService.getEntityById(batchId);

        log.info("Running rule-based risk analysis on batch {}", batch.getTrackingId());

        RiskAnalysisResult result = riskAnalyzer.analyze(batch);

        AIAnalysis analysis = new AIAnalysis();
        analysis.setBatch(batch);
        analysis.setRiskScore(result.getRiskScore());
        analysis.setRiskLevel(result.getRiskLevel());
        analysis.setDetectedAnomalies(
                result.getDetectedAnomalies().isEmpty()
                        ? null
                        : String.join("|", result.getDetectedAnomalies()));
        analysis.setRecommendations(result.getRecommendations());

        AIAnalysis saved = analysisRepository.save(analysis);

        // Send a notification for HIGH risk batches
        if (RiskLevel.HIGH.equals(result.getRiskLevel())) {
            User farmer = batch.getFarmer();
            notificationService.send(farmer,
                    "HIGH RISK alert on batch " + batch.getTrackingId()
                            + ". Score: " + result.getRiskScore() + "/100.",
                    NotificationType.ALERT);
        }

        return AIAnalysisDto.from(saved);
    }

    @Override
    public AIAnalysisDto analyze(Long batchId, User requestingUser) {
        batchService.assertCanAccessBatch(batchId, requestingUser);
        if (!UserRole.ADMIN.equals(requestingUser.getRole())
                && !UserRole.FARMER.equals(requestingUser.getRole())
                && !UserRole.WAREHOUSE_MANAGER.equals(requestingUser.getRole())) {
            throw new UnauthorizedAccessException("Your role cannot run risk analysis for this batch.");
        }
        return analyze(batchId);
    }

    @Override
    @Transactional(readOnly = true)
    public AIAnalysisDto getLatestForBatch(Long batchId) {
        ProductBatch batch = batchService.getEntityById(batchId);
        return analysisRepository.findFirstByBatchOrderByAnalyzedAtDesc(batch)
                .map(AIAnalysisDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("AIAnalysis", "batchId", batchId));
    }

    @Override
    @Transactional(readOnly = true)
    public AIAnalysisDto getLatestForBatch(Long batchId, User requestingUser) {
        batchService.assertCanAccessBatch(batchId, requestingUser);
        return getLatestForBatch(batchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AIAnalysisDto> getHistoryForBatch(Long batchId) {
        ProductBatch batch = batchService.getEntityById(batchId);
        return analysisRepository.findByBatchOrderByAnalyzedAtDesc(batch)
                .stream().map(AIAnalysisDto::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AIAnalysisDto> getHistoryForBatch(Long batchId, User requestingUser) {
        batchService.assertCanAccessBatch(batchId, requestingUser);
        return getHistoryForBatch(batchId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AIAnalysisDto> getAlerts(RiskLevel riskLevel, Pageable pageable) {
        return analysisRepository.findByRiskLevel(riskLevel, pageable).map(AIAnalysisDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AIAnalysisDto> getHighRiskAlerts() {
        return analysisRepository.findByRiskLevelOrderByAnalyzedAtDesc(RiskLevel.HIGH)
                .stream().map(AIAnalysisDto::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRiskLevel(RiskLevel riskLevel) {
        return analysisRepository.countByRiskLevel(riskLevel);
    }
}
