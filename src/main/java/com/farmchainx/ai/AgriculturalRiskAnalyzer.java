package com.farmchainx.ai;

import com.farmchainx.entity.ProductBatch;

/**
 * Contract for agricultural supply-chain risk analysis.
 *
 * The current implementation is a deterministic rule-based analyzer.
 * This interface is intentionally narrow so it can be backed by an
 * external ML model or AI API without changing callers.
 */
public interface AgriculturalRiskAnalyzer {

    /**
     * Analyses a product batch for supply-chain risk.
     *
     * @param batch the batch to analyse (fully loaded with supply-chain events,
     *              quality inspections, and transportation records)
     * @return an immutable {@link RiskAnalysisResult} containing score, level,
     *         detected anomalies, and recommendations
     */
    RiskAnalysisResult analyze(ProductBatch batch);
}
