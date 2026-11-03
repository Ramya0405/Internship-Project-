package com.farmchainx.service.impl;

import com.farmchainx.dto.TransportationDto;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.Transportation;
import com.farmchainx.entity.User;
import com.farmchainx.enums.EventType;
import com.farmchainx.enums.NotificationType;
import com.farmchainx.enums.TransportationStatus;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.repository.TransportationRepository;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.ProductBatchService;
import com.farmchainx.service.SupplyChainEventService;
import com.farmchainx.service.TransportationService;
import com.farmchainx.dto.SupplyChainEventDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransportationServiceImpl implements TransportationService {

    private final TransportationRepository    transportationRepository;
    private final ProductBatchService         batchService;
    private final SupplyChainEventService     eventService;
    private final NotificationService         notificationService;

    public TransportationServiceImpl(TransportationRepository transportationRepository,
                                     ProductBatchService batchService,
                                     SupplyChainEventService eventService,
                                     NotificationService notificationService) {
        this.transportationRepository = transportationRepository;
        this.batchService             = batchService;
        this.eventService             = eventService;
        this.notificationService      = notificationService;
    }

    @Override
    public TransportationDto create(TransportationDto dto, User actor) {
        if (actor.getRole() != UserRole.ADMIN && actor.getRole() != UserRole.DISTRIBUTOR) {
            throw new UnauthorizedAccessException("Only distributors or admins can create transportation records.");
        }
        ProductBatch batch = batchService.getEntityById(dto.getBatchId());
        if (!UserRole.ADMIN.equals(actor.getRole())) {
            batchService.assertCanAccessBatch(batch.getId(), actor);
        }

        Transportation transport = new Transportation();
        transport.setBatch(batch);
        transport.setDistributor(actor);
        transport.setSourceLocation(dto.getSourceLocation());
        transport.setDestinationLocation(dto.getDestinationLocation());
        transport.setDepartureDate(dto.getDepartureDate());
        transport.setEstimatedArrival(dto.getEstimatedArrival());
        transport.setVehicleNumber(dto.getVehicleNumber());
        transport.setNotes(dto.getNotes());
        transport.setTransportationStatus(TransportationStatus.SCHEDULED);

        Transportation saved = transportationRepository.save(transport);

        // Record a supply-chain event automatically
        SupplyChainEventDto event = new SupplyChainEventDto();
        event.setBatchId(batch.getId());
        event.setEventType(EventType.DISPATCHED);
        event.setLocation(dto.getSourceLocation());
        event.setDescription("Dispatched to: " + dto.getDestinationLocation()
                + (dto.getVehicleNumber() != null ? " via vehicle " + dto.getVehicleNumber() : ""));
        eventService.addEvent(event, actor);

        // Notify batch owner
        notificationService.send(batch.getFarmer(),
                "Your batch " + batch.getTrackingId() + " has been dispatched.",
                NotificationType.INFO);

        return TransportationDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransportationDto findById(Long id) {
        return TransportationDto.from(
                transportationRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Transportation", "id", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public TransportationDto findById(Long id, User requestingUser) {
        Transportation transport = transportationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transportation", "id", id));
        assertCanAccess(transport, requestingUser);
        return TransportationDto.from(transport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransportationDto> findByBatch(Long batchId) {
        ProductBatch batch = batchService.getEntityById(batchId);
        return transportationRepository.findByBatchOrderByDepartureDateDesc(batch)
                .stream().map(TransportationDto::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransportationDto> findByBatch(Long batchId, User requestingUser) {
        ProductBatch batch = batchService.getEntityById(batchId);
        batchService.assertCanAccessBatch(batchId, requestingUser);
        if (UserRole.DISTRIBUTOR.equals(requestingUser.getRole())) {
            return transportationRepository
                    .findByBatchAndDistributorOrderByDepartureDateDesc(batch, requestingUser)
                    .stream().map(TransportationDto::from).collect(Collectors.toList());
        }
        return transportationRepository.findByBatchOrderByDepartureDateDesc(batch)
                .stream().map(TransportationDto::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransportationDto> findByDistributor(User distributor, Pageable pageable) {
        if (UserRole.ADMIN.equals(distributor.getRole())) {
            return transportationRepository.findAll(pageable).map(TransportationDto::from);
        }
        if (UserRole.DISTRIBUTOR.equals(distributor.getRole())) {
            return transportationRepository.findByDistributor(distributor, pageable)
                    .map(TransportationDto::from);
        }
        throw new UnauthorizedAccessException(
                "Only distributors or admins can view transportation records.");
    }

    @Override
    public TransportationDto update(Long id, TransportationDto dto, User actor) {
        Transportation transport = transportationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transportation", "id", id));
        assertCanAccess(transport, actor);

        if (dto.getActualArrival()          != null) transport.setActualArrival(dto.getActualArrival());
        if (dto.getTransportationStatus()   != null) transport.setTransportationStatus(dto.getTransportationStatus());
        if (dto.getNotes()                  != null) transport.setNotes(dto.getNotes());
        if (dto.getVehicleNumber()          != null) transport.setVehicleNumber(dto.getVehicleNumber());

        // Auto-create ARRIVED supply-chain event when transport is completed
        if (dto.getTransportationStatus() == TransportationStatus.COMPLETED) {
            SupplyChainEventDto event = new SupplyChainEventDto();
            event.setBatchId(transport.getBatch().getId());
            event.setEventType(EventType.ARRIVED_AT_WAREHOUSE);
            event.setLocation(transport.getDestinationLocation());
            event.setDescription("Arrived at: " + transport.getDestinationLocation());
            eventService.addEvent(event, actor);
        }

        // Auto-create DELAY supply-chain event
        if (dto.getTransportationStatus() == TransportationStatus.DELAYED) {
            SupplyChainEventDto event = new SupplyChainEventDto();
            event.setBatchId(transport.getBatch().getId());
            event.setEventType(EventType.DELAY_REPORTED);
            event.setLocation(transport.getDestinationLocation());
            event.setDescription("Transportation delay reported. " + (dto.getNotes() != null ? dto.getNotes() : ""));
            eventService.addEvent(event, actor);

            notificationService.send(transport.getBatch().getFarmer(),
                    "Delay reported for batch " + transport.getBatch().getTrackingId(),
                    NotificationType.WARNING);
        }

        return TransportationDto.from(transportationRepository.save(transport));
    }

    private void assertCanAccess(Transportation transport, User actor) {
        if (UserRole.ADMIN.equals(actor.getRole())) {
            return;
        }
        if (actor.getRole() != UserRole.DISTRIBUTOR
                || transport.getDistributor() == null
                || !transport.getDistributor().getId().equals(actor.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to access this transportation record.");
        }
    }
}
