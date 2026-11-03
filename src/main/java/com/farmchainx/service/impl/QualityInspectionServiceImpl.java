package com.farmchainx.service.impl;

import com.farmchainx.dto.QualityInspectionDto;
import com.farmchainx.dto.SupplyChainEventDto;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.QualityInspection;
import com.farmchainx.entity.User;
import com.farmchainx.enums.EventType;
import com.farmchainx.enums.NotificationType;
import com.farmchainx.enums.QualityGrade;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.repository.QualityInspectionRepository;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.ProductBatchService;
import com.farmchainx.service.QualityInspectionService;
import com.farmchainx.service.SupplyChainEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QualityInspectionServiceImpl implements QualityInspectionService {

    private final QualityInspectionRepository inspectionRepository;
    private final ProductBatchService         batchService;
    private final SupplyChainEventService     eventService;
    private final NotificationService         notificationService;

    public QualityInspectionServiceImpl(QualityInspectionRepository inspectionRepository,
                                        ProductBatchService batchService,
                                        SupplyChainEventService eventService,
                                        NotificationService notificationService) {
        this.inspectionRepository = inspectionRepository;
        this.batchService         = batchService;
        this.eventService         = eventService;
        this.notificationService  = notificationService;
    }

    @Override
    public QualityInspectionDto create(QualityInspectionDto dto, User inspector) {
        if (inspector.getRole() != UserRole.ADMIN && inspector.getRole() != UserRole.WAREHOUSE_MANAGER) {
            throw new UnauthorizedAccessException("Only warehouse managers or admins can inspect batches.");
        }
        ProductBatch batch = batchService.getEntityById(dto.getBatchId());
        if (!UserRole.ADMIN.equals(inspector.getRole())) {
            batchService.assertCanAccessBatch(batch.getId(), inspector);
        }

        QualityInspection qi = new QualityInspection();
        qi.setBatch(batch);
        qi.setInspectedBy(inspector);
        qi.setQualityGrade(dto.getQualityGrade());
        qi.setTemperature(dto.getTemperature());
        qi.setHumidity(dto.getHumidity());
        qi.setInspectionNotes(dto.getInspectionNotes());
        qi.setInspectionDate(dto.getInspectionDate());

        QualityInspection saved = inspectionRepository.save(qi);

        // Record supply-chain event
        SupplyChainEventDto event = new SupplyChainEventDto();
        event.setBatchId(batch.getId());
        event.setEventType(EventType.QUALITY_CHECK);
        event.setTemperature(dto.getTemperature());
        event.setHumidity(dto.getHumidity());
        event.setDescription("Quality inspection completed. Grade: " + dto.getQualityGrade()
                + (dto.getInspectionNotes() != null ? ". Notes: " + dto.getInspectionNotes() : ""));
        eventService.addEvent(event, inspector);

        // Alert on rejection
        if (QualityGrade.REJECTED.equals(dto.getQualityGrade())) {
            notificationService.send(batch.getFarmer(),
                    "ALERT: Batch " + batch.getTrackingId() + " has FAILED quality inspection.",
                    NotificationType.ALERT);
        }

        return QualityInspectionDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public QualityInspectionDto findById(Long id) {
        return QualityInspectionDto.from(
                inspectionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("QualityInspection", "id", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityInspectionDto> findByBatch(Long batchId) {
        ProductBatch batch = batchService.getEntityById(batchId);
        return inspectionRepository.findByBatchOrderByInspectionDateDesc(batch)
                .stream().map(QualityInspectionDto::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityInspectionDto> findByBatch(Long batchId, User requestingUser) {
        batchService.assertCanAccessBatch(batchId, requestingUser);
        return findByBatch(batchId);
    }

    @Override
    @Transactional(readOnly = true)
    public QualityInspectionDto findLatestForBatch(Long batchId) {
        ProductBatch batch = batchService.getEntityById(batchId);
        return inspectionRepository.findFirstByBatchOrderByInspectionDateDesc(batch)
                .map(QualityInspectionDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("QualityInspection", "batchId", batchId));
    }
}
