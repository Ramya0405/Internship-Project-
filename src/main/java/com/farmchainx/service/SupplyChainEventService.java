package com.farmchainx.service;

import com.farmchainx.dto.SupplyChainEventDto;
import com.farmchainx.entity.User;

import java.util.List;

public interface SupplyChainEventService {

    SupplyChainEventDto addEvent(SupplyChainEventDto dto, User actor);

    List<SupplyChainEventDto> getHistoryForBatch(Long batchId);

    List<SupplyChainEventDto> getHistoryForBatch(Long batchId, User requestingUser);

    List<SupplyChainEventDto> getHistoryForTrackingId(String trackingId);
}
