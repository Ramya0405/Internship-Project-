package com.farmchainx.service.impl;

import com.farmchainx.dto.SupplyChainEventDto;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.SupplyChainEvent;
import com.farmchainx.entity.User;
import com.farmchainx.enums.BatchStatus;
import com.farmchainx.enums.EventType;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.repository.SupplyChainEventRepository;
import com.farmchainx.service.ProductBatchService;
import com.farmchainx.service.SupplyChainEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplyChainEventServiceImpl implements SupplyChainEventService {

    private final SupplyChainEventRepository eventRepository;
    private final ProductBatchService        batchService;

    public SupplyChainEventServiceImpl(SupplyChainEventRepository eventRepository,
                                       ProductBatchService batchService) {
        this.eventRepository = eventRepository;
        this.batchService    = batchService;
    }

    @Override
    public SupplyChainEventDto addEvent(SupplyChainEventDto dto, User actor) {
        ProductBatch batch = batchService.getEntityById(dto.getBatchId());
        assertCanRecordEvent(batch, dto.getEventType(), actor);

        SupplyChainEvent event = new SupplyChainEvent();
        event.setBatch(batch);
        event.setEventType(dto.getEventType());
        event.setLocation(dto.getLocation());
        event.setDescription(dto.getDescription());
        event.setUpdatedBy(actor);
        event.setTemperature(dto.getTemperature());
        event.setHumidity(dto.getHumidity());
        event.setEventTimestamp(
                dto.getEventTimestamp() != null ? dto.getEventTimestamp() : LocalDateTime.now());

        return SupplyChainEventDto.from(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplyChainEventDto> getHistoryForBatch(Long batchId) {
        ProductBatch batch = batchService.getEntityById(batchId);
        return eventRepository.findByBatchOrderByEventTimestampAsc(batch)
                .stream().map(SupplyChainEventDto::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplyChainEventDto> getHistoryForBatch(Long batchId, User requestingUser) {
        batchService.assertCanAccessBatch(batchId, requestingUser);
        return getHistoryForBatch(batchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplyChainEventDto> getHistoryForTrackingId(String trackingId) {
        ProductBatch batch = batchService.getEntityByTrackingId(trackingId);
        return eventRepository.findByBatchOrderByEventTimestampAsc(batch)
                .stream().map(SupplyChainEventDto::from).collect(Collectors.toList());
    }

    private void assertCanRecordEvent(ProductBatch batch, EventType eventType, User actor) {
        if (UserRole.ADMIN.equals(actor.getRole())) {
            return;
        }

        boolean permitted = switch (actor.getRole()) {
            case FARMER -> batch.getFarmer().getId().equals(actor.getId())
                    && (eventType == EventType.HARVESTED || eventType == EventType.PACKED);
            case DISTRIBUTOR -> batch.getCurrentStatus() == BatchStatus.IN_TRANSIT
                    && (eventType == EventType.DISPATCHED
                    || eventType == EventType.IN_TRANSIT
                    || eventType == EventType.ARRIVED_AT_WAREHOUSE
                    || eventType == EventType.DELAY_REPORTED
                    || eventType == EventType.TEMPERATURE_ALERT
                    || eventType == EventType.HUMIDITY_ALERT
                    || eventType == EventType.DAMAGED);
            case WAREHOUSE_MANAGER -> batch.getCurrentStatus() == BatchStatus.IN_WAREHOUSE
                    && (eventType == EventType.ARRIVED_AT_WAREHOUSE
                    || eventType == EventType.STORED
                    || eventType == EventType.QUALITY_CHECK
                    || eventType == EventType.TEMPERATURE_ALERT
                    || eventType == EventType.HUMIDITY_ALERT
                    || eventType == EventType.DAMAGED
                    || eventType == EventType.DISPATCHED_TO_RETAILER);
            case RETAILER -> (batch.getCurrentStatus() == BatchStatus.QUALITY_INSPECTED
                    || batch.getCurrentStatus() == BatchStatus.AT_RETAILER)
                    && (eventType == EventType.ARRIVED_AT_RETAILER
                    || eventType == EventType.SOLD
                    || eventType == EventType.DAMAGED);
            default -> false;
        };
        if (!permitted) {
            throw new UnauthorizedAccessException("Your role cannot record this event for the batch.");
        }
    }
}
