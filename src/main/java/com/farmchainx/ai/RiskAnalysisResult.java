package com.farmchainx.ai;

import com.farmchainx.enums.RiskLevel;

import java.util.List;

/**
 * Immutable result of a risk analysis run.
 */
public class RiskAnalysisResult {

    private final int riskScore;
    private final RiskLevel riskLevel;
    private final List<String> detectedAnomalies;
    private final String recommendations;

    public RiskAnalysisResult(int riskScore, List<String> detectedAnomalies, String recommendations) {
        this.riskScore         = Math.min(100, Math.max(0, riskScore));
        this.riskLevel         = classifyScore(this.riskScore);
        this.detectedAnomalies = List.copyOf(detectedAnomalies);
        this.recommendations   = recommendations;
    }

    private static RiskLevel classifyScore(int score) {
        if (score <= 30) return RiskLevel.LOW;
        if (score <= 60) return RiskLevel.MEDIUM;
        return RiskLevel.HIGH;
    }

    public int getRiskScore()                 { return riskScore; }
    public RiskLevel getRiskLevel()           { return riskLevel; }
    public List<String> getDetectedAnomalies(){ return detectedAnomalies; }
    public String getRecommendations()        { return recommendations; }
}
