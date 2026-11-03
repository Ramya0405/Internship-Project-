package com.farmchainx.repository;

import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.QualityInspection;
import com.farmchainx.enums.QualityGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QualityInspectionRepository extends JpaRepository<QualityInspection, Long> {

    List<QualityInspection> findByBatchOrderByInspectionDateDesc(ProductBatch batch);

    /** Most recent inspection for a batch. */
    Optional<QualityInspection> findFirstByBatchOrderByInspectionDateDesc(ProductBatch batch);

    long countByQualityGrade(QualityGrade grade);

    long countByBatch(ProductBatch batch);

    /** Check whether any inspection on this batch was rejected. */
    boolean existsByBatchAndQualityGrade(ProductBatch batch, QualityGrade grade);
}
