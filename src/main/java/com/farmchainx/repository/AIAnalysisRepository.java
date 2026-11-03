package com.farmchainx.repository;

import com.farmchainx.entity.AIAnalysis;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.enums.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIAnalysisRepository extends JpaRepository<AIAnalysis, Long> {

    List<AIAnalysis> findByBatchOrderByAnalyzedAtDesc(ProductBatch batch);

    /** Most recent analysis result for a batch. */
    Optional<AIAnalysis> findFirstByBatchOrderByAnalyzedAtDesc(ProductBatch batch);

    Page<AIAnalysis> findByRiskLevel(RiskLevel riskLevel, Pageable pageable);

    List<AIAnalysis> findByRiskLevelOrderByAnalyzedAtDesc(RiskLevel riskLevel);

    long countByRiskLevel(RiskLevel riskLevel);
}
