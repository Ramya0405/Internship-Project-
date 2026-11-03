package com.farmchainx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmchainx.dto.BatchTrackingResponse;
import com.farmchainx.dto.SupplyChainEventDto;
import com.farmchainx.entity.Farm;
import com.farmchainx.entity.Product;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.User;
import com.farmchainx.enums.EventType;
import com.farmchainx.service.impl.BatchTrackingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchTrackingServiceTest {

    @Mock
    private ProductBatchService batchService;

    @Mock
    private SupplyChainEventService eventService;

    @InjectMocks
    private BatchTrackingServiceImpl trackingService;

    @Test
    void publicTrackingResponseDoesNotSerializePrivatePartyOrLogisticsDetails() throws Exception {
        User farmer = new User();
        farmer.setFullName("Private Farmer Name");
        Product product = new Product();
        product.setProductName("Tomatoes");
        Farm farm = new Farm();
        farm.setFarmName("Private Farm");
        farm.setLocation("Private Farm Location");

        ProductBatch batch = new ProductBatch();
        batch.setTrackingId("FCX-TEST-1");
        batch.setProduct(product);
        batch.setFarm(farm);
        batch.setFarmer(farmer);
        when(batchService.getEntityByTrackingId("FCX-TEST-1")).thenReturn(batch);

        SupplyChainEventDto event = new SupplyChainEventDto();
        event.setEventType(EventType.HARVESTED);
        event.setEventTimestamp(LocalDateTime.of(2026, 1, 1, 10, 0));
        event.setLocation("Private Warehouse");
        event.setDescription("Private Transportation Details");
        event.setUpdatedByName("Private Farmer Name");
        when(eventService.getHistoryForBatch(batch.getId())).thenReturn(List.of(event));

        BatchTrackingResponse response = trackingService.getFullJourney("FCX-TEST-1");
        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(response);

        assertThat(response.getEvents()).hasSize(1);
        assertThat(json).contains("HARVESTED", "FCX-TEST-1", "Tomatoes")
                .doesNotContain("Private Farmer Name", "Private Farm", "Private Farm Location",
                        "Private Warehouse", "Private Transportation Details", "transportations",
                        "qualityInspections");
    }
}
