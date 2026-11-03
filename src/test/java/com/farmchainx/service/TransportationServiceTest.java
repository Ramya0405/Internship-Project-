package com.farmchainx.service;

import com.farmchainx.TestEntityHelper;
import com.farmchainx.dto.SupplyChainEventDto;
import com.farmchainx.dto.TransportationDto;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.Transportation;
import com.farmchainx.entity.User;
import com.farmchainx.enums.EventType;
import com.farmchainx.enums.NotificationType;
import com.farmchainx.enums.TransportationStatus;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.repository.TransportationRepository;
import com.farmchainx.service.impl.TransportationServiceImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportationServiceTest {

    @Mock
    private TransportationRepository transportationRepository;

    @Mock
    private ProductBatchService batchService;

    @Mock
    private SupplyChainEventService eventService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransportationServiceImpl transportationService;

    private User distributor;
    private User farmer;
    private ProductBatch batch;
    private Transportation transportation;

    @BeforeEach
    void setUp() {
        distributor = new User();
        TestEntityHelper.withId(distributor, 10L);
        distributor.setFullName("Express Logistics");
        distributor.setRole(UserRole.DISTRIBUTOR);

        farmer = new User();
        TestEntityHelper.withId(farmer, 20L);
        farmer.setFullName("John Farmer");
        farmer.setRole(UserRole.FARMER);

        batch = new ProductBatch();
        TestEntityHelper.withId(batch, 100L);
        batch.setTrackingId("FCX-20240501-TEST1234");
        batch.setFarmer(farmer);

        transportation = new Transportation();
        TestEntityHelper.withId(transportation, 1L);
        transportation.setBatch(batch);
        transportation.setDistributor(distributor);
        transportation.setSourceLocation("Punjab Farm #3");
        transportation.setDestinationLocation("Delhi Central Depot");
        transportation.setTransportationStatus(TransportationStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Create transportation automatically adds DISPATCHED event and notifies farmer")
    void create_success() {
        TransportationDto dto = new TransportationDto();
        dto.setBatchId(100L);
        dto.setSourceLocation("Punjab Farm #3");
        dto.setDestinationLocation("Delhi Central Depot");
        dto.setDepartureDate(LocalDateTime.now());
        dto.setVehicleNumber("DL01AB9999");

        when(batchService.getEntityById(100L)).thenReturn(batch);
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(i -> {
            Transportation t = i.getArgument(0);
            TestEntityHelper.withId(t, 1L);
            return t;
        });

        TransportationDto created = transportationService.create(dto, distributor);

        assertThat(created).isNotNull();
        assertThat(created.getSourceLocation()).isEqualTo("Punjab Farm #3");
        assertThat(created.getDestinationLocation()).isEqualTo("Delhi Central Depot");
        assertThat(created.getTransportationStatus()).isEqualTo(TransportationStatus.SCHEDULED);

        // Verify automated DISPATCHED supply chain event
        verify(eventService).addEvent(argThat(ev ->
                ev.getBatchId().equals(100L) && ev.getEventType() == EventType.DISPATCHED
        ), eq(distributor));

        // Verify notification to batch farmer
        verify(notificationService).send(eq(farmer), contains("dispatched"), eq(NotificationType.INFO));
    }

    @Test
    @DisplayName("Update status to DELAYED triggers delay event and warning notification")
    void update_delayed_triggersWarning() {
        when(transportationRepository.findById(1L)).thenReturn(Optional.of(transportation));
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(i -> i.getArgument(0));

        TransportationDto updateDto = new TransportationDto();
        updateDto.setTransportationStatus(TransportationStatus.DELAYED);
        updateDto.setNotes("Vehicle tyre puncture on highway");

        TransportationDto result = transportationService.update(1L, updateDto, distributor);

        assertThat(result.getTransportationStatus()).isEqualTo(TransportationStatus.DELAYED);

        // Verify delay event
        verify(eventService).addEvent(argThat(ev ->
                ev.getEventType() == EventType.DELAY_REPORTED
        ), eq(distributor));

        // Verify warning notification to farmer
        verify(notificationService).send(eq(farmer), contains("Delay reported"), eq(NotificationType.WARNING));
    }

    @Test
    @DisplayName("Update status to COMPLETED records arrival event")
    void update_completed_recordsArrival() {
        when(transportationRepository.findById(1L)).thenReturn(Optional.of(transportation));
        when(transportationRepository.save(any(Transportation.class))).thenAnswer(i -> i.getArgument(0));

        TransportationDto updateDto = new TransportationDto();
        updateDto.setTransportationStatus(TransportationStatus.COMPLETED);
        updateDto.setActualArrival(LocalDateTime.now());

        TransportationDto result = transportationService.update(1L, updateDto, distributor);

        assertThat(result.getTransportationStatus()).isEqualTo(TransportationStatus.COMPLETED);
        verify(eventService).addEvent(argThat(ev ->
                ev.getEventType() == EventType.ARRIVED_AT_WAREHOUSE && ev.getDescription().contains("Arrived at")
        ), eq(distributor));
    }

    @Test
    @DisplayName("Distributor cannot update another distributor's transportation")
    void update_notOwner_throwsException() {
        User otherDistributor = new User();
        TestEntityHelper.withId(otherDistributor, 11L);
        otherDistributor.setRole(UserRole.DISTRIBUTOR);
        when(transportationRepository.findById(1L)).thenReturn(Optional.of(transportation));

        TransportationDto updateDto = new TransportationDto();
        updateDto.setTransportationStatus(TransportationStatus.DELAYED);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> transportationService.update(1L, updateDto, otherDistributor))
                .isInstanceOf(UnauthorizedAccessException.class);
        verify(transportationRepository, never()).save(any(Transportation.class));
    }

    @Test
    @DisplayName("Only distributors and admins can list transportation records")
    void findByDistributor_unauthorizedRole_throwsException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> transportationService.findByDistributor(farmer, PageRequest.of(0, 10)))
                .isInstanceOf(UnauthorizedAccessException.class);
        verify(transportationRepository, never()).findByDistributor(any(User.class), any());
        verify(transportationRepository, never()).findAll(any(PageRequest.class));
    }
}
