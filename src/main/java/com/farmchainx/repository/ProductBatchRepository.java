package com.farmchainx.repository;

import com.farmchainx.entity.Farm;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.User;
import com.farmchainx.enums.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    Optional<ProductBatch> findByTrackingId(String trackingId);

    boolean existsByTrackingId(String trackingId);

    Page<ProductBatch> findByFarmer(User farmer, Pageable pageable);

    Page<ProductBatch> findByFarmerAndCurrentStatus(User farmer, BatchStatus status, Pageable pageable);

    Page<ProductBatch> findByFarm(Farm farm, Pageable pageable);

    Page<ProductBatch> findByCurrentStatus(BatchStatus status, Pageable pageable);

    List<ProductBatch> findByCurrentStatus(BatchStatus status);

    /** Find batches that have passed their expiry date and are not yet marked expired. */
    @Query("SELECT pb FROM ProductBatch pb WHERE pb.expiryDate < :today AND pb.currentStatus NOT IN ('EXPIRED','RECALLED','SOLD')")
    List<ProductBatch> findExpiredBatches(@Param("today") LocalDate today);

    Page<ProductBatch> findByTrackingIdContainingIgnoreCase(String trackingId, Pageable pageable);

    long countByCurrentStatus(BatchStatus status);

    long countByFarmer(User farmer);
}
