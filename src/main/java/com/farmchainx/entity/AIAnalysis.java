package com.farmchainx.entity;

import com.farmchainx.enums.RiskLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Stores the result of a rule-based agricultural risk analysis
 * performed by the AI analysis module against a product batch.
 */
@Entity
@Table(
    name = "ai_analysis",
    indexes = {
        @Index(name = "idx_ai_batch",      columnList = "batch_id"),
        @Index(name = "idx_ai_risk_level", columnList = "risk_level"),
        @Index(name = "idx_ai_analyzed_at",columnList = "analyzed_at")
    }
)
public class AIAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Batch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private ProductBatch batch;

    @Min(0) @Max(100)
    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 10)
    private RiskLevel riskLevel;

    /** Comma-separated list of anomaly descriptions detected. */
    @Column(name = "detected_anomalies", columnDefinition = "TEXT")
    private String detectedAnomalies;

    /** Human-readable recommendations produced by the analyzer. */
    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "analyzed_at", nullable = false, updatable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        this.analyzedAt = LocalDateTime.now();
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    public AIAnalysis() {}

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public ProductBatch getBatch() { return batch; }
    public void setBatch(ProductBatch batch) { this.batch = batch; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getDetectedAnomalies() { return detectedAnomalies; }
    public void setDetectedAnomalies(String detectedAnomalies) { this.detectedAnomalies = detectedAnomalies; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}
