package com.farmchainx.service;

import com.farmchainx.dto.QualityInspectionDto;
import com.farmchainx.entity.User;

import java.util.List;

public interface QualityInspectionService {

    QualityInspectionDto create(QualityInspectionDto dto, User inspector);

    QualityInspectionDto findById(Long id);

    List<QualityInspectionDto> findByBatch(Long batchId);

    List<QualityInspectionDto> findByBatch(Long batchId, User requestingUser);

    QualityInspectionDto findLatestForBatch(Long batchId);
}
