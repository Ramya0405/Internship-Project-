package com.farmchainx.service;

import com.farmchainx.dto.ProductBatchDto;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.User;
import com.farmchainx.enums.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductBatchService {

    ProductBatchDto create(ProductBatchDto dto, User farmer);

    ProductBatchDto findById(Long id);

    ProductBatchDto findById(Long id, User requestingUser);

    ProductBatch getEntityById(Long id);

    ProductBatchDto findByTrackingId(String trackingId);

    ProductBatchDto findByTrackingId(String trackingId, User requestingUser);

    ProductBatch getEntityByTrackingId(String trackingId);

    Page<ProductBatchDto> findAll(Pageable pageable);

    Page<ProductBatchDto> findByFarmer(User farmer, Pageable pageable);

    Page<ProductBatchDto> findByStatus(BatchStatus status, Pageable pageable);

    Page<ProductBatchDto> findByStatus(BatchStatus status, User requestingUser, Pageable pageable);

    void assertCanAccessBatch(Long batchId, User requestingUser);

    ProductBatchDto updateStatus(Long id, BatchStatus newStatus, User requestingUser);

    long countByStatus(BatchStatus status);

    long countByFarmer(User farmer);
}
