package com.farmchainx.service;

import com.farmchainx.TestEntityHelper;
import com.farmchainx.dto.SupplyChainEventDto;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.SupplyChainEvent;
import com.farmchainx.entity.User;
import com.farmchainx.enums.BatchStatus;
import com.farmchainx.enums.EventType;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.repository.ProductBatchRepository;
import com.farmchainx.repository.SupplyChainEventRepository;
import com.farmchainx.service.impl.ProductBatchServiceImpl;
import com.farmchainx.service.impl.SupplyChainEventServiceImpl;
import com.farmchainx.util.TrackingIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplyChainEventServiceTest {

    @Mock
    private SupplyChainEventRepository eventRepository;

    @Mock
    private ProductBatchService batchService;

    @Mock
    private ProductBatchRepository batchRepository;

    @Mock
    private ProductService productService;

    @Mock
    private FarmService farmService;

    @InjectMocks
    private SupplyChainEventServiceImpl eventService;

    private User farmer;
    private ProductBatch batch;

    @BeforeEach
    void setUp() {
        farmer = new User();
        TestEntityHelper.withId(farmer, 10L);
        farmer.setRole(UserRole.FARMER);

        batch = new ProductBatch();
        TestEntityHelper.withId(batch, 500L);
        batch.setFarmer(farmer);
        batch.setCurrentStatus(BatchStatus.CREATED);
    }

    @Test
    void addEvent_ownerCanRecordHarvest() {
        SupplyChainEventDto request = new SupplyChainEventDto();
        request.setBatchId(500L);
        request.setEventType(EventType.HARVESTED);
        when(batchService.getEntityById(500L)).thenReturn(batch);
        when(eventRepository.save(any(SupplyChainEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        eventService.addEvent(request, farmer);

        verify(eventRepository).save(argThat(event ->
                event.getEventType() == EventType.HARVESTED
                        && event.getUpdatedBy().equals(farmer)));
    }

    @Test
    void addEvent_nonOwnerCannotRecordHarvest() {
        User otherFarmer = new User();
        TestEntityHelper.withId(otherFarmer, 11L);
        otherFarmer.setRole(UserRole.FARMER);

        SupplyChainEventDto request = new SupplyChainEventDto();
        request.setBatchId(500L);
        request.setEventType(EventType.HARVESTED);
        when(batchService.getEntityById(500L)).thenReturn(batch);

        assertThatThrownBy(() -> eventService.addEvent(request, otherFarmer))
                .isInstanceOf(UnauthorizedAccessException.class);
        verify(eventRepository, never()).save(any(SupplyChainEvent.class));
    }

    @Test
    void getHistoryForBatch_rejectsUnrelatedUser() {
        User consumer = new User();
        TestEntityHelper.withId(consumer, 12L);
        consumer.setRole(UserRole.CONSUMER);
        when(batchRepository.findById(500L)).thenReturn(Optional.of(batch));

        SupplyChainEventServiceImpl serviceWithRealBatchAuthorization = createServiceWithRealBatchAuthorization();
        assertThatThrownBy(() -> serviceWithRealBatchAuthorization.getHistoryForBatch(500L, consumer))
                .isInstanceOf(UnauthorizedAccessException.class);
        verify(eventRepository, never()).findByBatchOrderByEventTimestampAsc(any());
    }

    @Test
    void getHistoryForBatch_rejectsDifferentFarmer() {
        User otherFarmer = new User();
        TestEntityHelper.withId(otherFarmer, 11L);
        otherFarmer.setRole(UserRole.FARMER);
        when(batchRepository.findById(500L)).thenReturn(Optional.of(batch));

        SupplyChainEventServiceImpl serviceWithRealBatchAuthorization = createServiceWithRealBatchAuthorization();
        assertThatThrownBy(() -> serviceWithRealBatchAuthorization.getHistoryForBatch(500L, otherFarmer))
                .isInstanceOf(UnauthorizedAccessException.class);
        verify(eventRepository, never()).findByBatchOrderByEventTimestampAsc(any());
    }

    private SupplyChainEventServiceImpl createServiceWithRealBatchAuthorization() {
        ProductBatchServiceImpl realBatchService = new ProductBatchServiceImpl(
                batchRepository, productService, farmService, new TrackingIdGenerator());
        return new SupplyChainEventServiceImpl(eventRepository, realBatchService);
    }
}
