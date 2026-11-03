package com.farmchainx.service.impl;

import com.farmchainx.dto.BatchTrackingResponse;
import com.farmchainx.dto.PublicTrackingEventDto;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.service.ProductBatchService;
import com.farmchainx.service.SupplyChainEventService;
import com.farmchainx.service.BatchTrackingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BatchTrackingServiceImpl implements BatchTrackingService {

    private final ProductBatchService     batchService;
    private final SupplyChainEventService eventService;
    public BatchTrackingServiceImpl(ProductBatchService batchService,
                                    SupplyChainEventService eventService) {
        this.batchService          = batchService;
        this.eventService          = eventService;
    }

    @Override
    public BatchTrackingResponse getFullJourney(String trackingId) {
        ProductBatch batch = batchService.getEntityByTrackingId(trackingId);

        BatchTrackingResponse response = new BatchTrackingResponse();

        // Batch + product
        response.setTrackingId(batch.getTrackingId());
        response.setCurrentStatus(batch.getCurrentStatus());
        response.setQuantity(batch.getQuantity());
        response.setHarvestDate(batch.getHarvestDate());
        response.setExpiryDate(batch.getExpiryDate());

        if (batch.getProduct() != null) {
            response.setProductName(batch.getProduct().getProductName());
            response.setProductCategory(batch.getProduct().getCategory());
        }

        // Expose only event type and time; event location, description, actor,
        // inspection, and transportation fields are private operational data.
        response.setEvents(eventService.getHistoryForBatch(batch.getId()).stream()
                .map(event -> new PublicTrackingEventDto(event.getEventType(), event.getEventTimestamp()))
                .toList());

        return response;
    }
}
