package com.farmchainx.service;

import com.farmchainx.dto.TransportationDto;
import com.farmchainx.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransportationService {

    TransportationDto create(TransportationDto dto, User actor);

    TransportationDto findById(Long id);

    TransportationDto findById(Long id, User requestingUser);

    List<TransportationDto> findByBatch(Long batchId);

    List<TransportationDto> findByBatch(Long batchId, User requestingUser);

    Page<TransportationDto> findByDistributor(User distributor, Pageable pageable);

    TransportationDto update(Long id, TransportationDto dto, User actor);
}
