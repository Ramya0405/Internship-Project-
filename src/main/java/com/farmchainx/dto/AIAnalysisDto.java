package com.farmchainx.dto;

import com.farmchainx.entity.AIAnalysis;
import com.farmchainx.enums.RiskLevel;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * DTO for AI analysis results.
 */
public class AIAnalysisDto {

    private Long id;
    private Long batchId;
    private String trackingId;
    private Integer riskScore;
    private RiskLevel riskLevel;
    private List<String> detectedAnomalies;
    private String recommendations;
    private LocalDateTime analyzedAt;

    public AIAnalysisDto() {}

    public static AIAnalysisDto from(AIAnalysis a) {
        AIAnalysisDto dto = new AIAnalysisDto();
        dto.id          = a.getId();
        dto.batchId     = a.getBatch() != null ? a.getBatch().getId()          : null;
        dto.trackingId  = a.getBatch() != null ? a.getBatch().getTrackingId()  : null;
        dto.riskScore   = a.getRiskScore();
        dto.riskLevel   = a.getRiskLevel();
        dto.recommendations = a.getRecommendations();
        dto.analyzedAt  = a.getAnalyzedAt();

        // Anomalies are stored comma-separated in DB
        if (a.getDetectedAnomalies() != null && !a.getDetectedAnomalies().isBlank()) {
            dto.detectedAnomalies = Arrays.asList(a.getDetectedAnomalies().split("\\|"));
        } else {
            dto.detectedAnomalies = List.of();
        }
        return dto;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public List<String> getDetectedAnomalies() { return detectedAnomalies; }
    public void setDetectedAnomalies(List<String> detectedAnomalies) { this.detectedAnomalies = detectedAnomalies; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}
