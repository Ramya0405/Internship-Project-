package com.farmchainx.service.impl;

import com.farmchainx.dto.ProductBatchDto;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.Farm;
import com.farmchainx.entity.User;
import com.farmchainx.enums.BatchStatus;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.InvalidBatchStatusException;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.repository.ProductBatchRepository;
import com.farmchainx.service.FarmService;
import com.farmchainx.service.ProductBatchService;
import com.farmchainx.service.ProductService;
import com.farmchainx.util.TrackingIdGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class ProductBatchServiceImpl implements ProductBatchService {

    // Valid forward transitions for each status
    private static final Map<BatchStatus, Set<BatchStatus>> TRANSITIONS = Map.of(
        BatchStatus.CREATED,            EnumSet.of(BatchStatus.IN_TRANSIT,        BatchStatus.RECALLED),
        BatchStatus.IN_TRANSIT,         EnumSet.of(BatchStatus.IN_WAREHOUSE,      BatchStatus.RECALLED, BatchStatus.EXPIRED),
        BatchStatus.IN_WAREHOUSE,       EnumSet.of(BatchStatus.QUALITY_INSPECTED, BatchStatus.RECALLED, BatchStatus.EXPIRED),
        BatchStatus.QUALITY_INSPECTED,  EnumSet.of(BatchStatus.AT_RETAILER,       BatchStatus.RECALLED, BatchStatus.EXPIRED),
        BatchStatus.AT_RETAILER,        EnumSet.of(BatchStatus.SOLD,              BatchStatus.RECALLED, BatchStatus.EXPIRED),
        BatchStatus.SOLD,               EnumSet.noneOf(BatchStatus.class),
        BatchStatus.RECALLED,           EnumSet.noneOf(BatchStatus.class),
        BatchStatus.EXPIRED,            EnumSet.noneOf(BatchStatus.class)
    );

    private final ProductBatchRepository batchRepository;
    private final ProductService         productService;
    private final FarmService            farmService;
    private final TrackingIdGenerator    trackingIdGenerator;

    public ProductBatchServiceImpl(ProductBatchRepository batchRepository,
                                   ProductService productService,
                                   FarmService farmService,
                                   TrackingIdGenerator trackingIdGenerator) {
        this.batchRepository     = batchRepository;
        this.productService      = productService;
        this.farmService         = farmService;
        this.trackingIdGenerator = trackingIdGenerator;
    }

    @Override
    public ProductBatchDto create(ProductBatchDto dto, User farmer) {
        if (farmer.getRole() != UserRole.FARMER && farmer.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedAccessException("Only farmers or admins can create batches.");
        }

        ProductBatch batch = new ProductBatch();

        // Generate unique tracking ID, retry if collision (extremely unlikely)
        String trackingId;
        int attempts = 0;
        do {
            trackingId = trackingIdGenerator.generate();
            attempts++;
            if (attempts > 10) throw new IllegalStateException("Could not generate unique tracking ID");
        } while (batchRepository.existsByTrackingId(trackingId));

        batch.setTrackingId(trackingId);
        batch.setProduct(productService.getEntityById(dto.getProductId()));
        Farm farm = farmService.getEntityById(dto.getFarmId());
        if (!UserRole.ADMIN.equals(farmer.getRole())
                && !farm.getOwner().getId().equals(farmer.getId())) {
            throw new UnauthorizedAccessException("You can only create batches for your own farms.");
        }
        batch.setFarm(farm);
        batch.setFarmer(farmer);
        batch.setQuantity(dto.getQuantity());
        batch.setHarvestDate(dto.getHarvestDate());
        batch.setExpiryDate(dto.getExpiryDate());
        batch.setCurrentStatus(BatchStatus.CREATED);

        return ProductBatchDto.from(batchRepository.save(batch));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBatchDto findById(Long id) {
        return ProductBatchDto.from(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBatchDto findById(Long id, User requestingUser) {
        ProductBatch batch = getEntityById(id);
        assertCanAccessBatch(batch, requestingUser);
        return ProductBatchDto.from(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBatch getEntityById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBatch", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBatchDto findByTrackingId(String trackingId) {
        return ProductBatchDto.from(getEntityByTrackingId(trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBatchDto findByTrackingId(String trackingId, User requestingUser) {
        ProductBatch batch = getEntityByTrackingId(trackingId);
        assertCanAccessBatch(batch, requestingUser);
        return ProductBatchDto.from(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBatch getEntityByTrackingId(String trackingId) {
        return batchRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBatch", "trackingId", trackingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductBatchDto> findAll(Pageable pageable) {
        return batchRepository.findAll(pageable).map(ProductBatchDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductBatchDto> findByFarmer(User farmer, Pageable pageable) {
        return batchRepository.findByFarmer(farmer, pageable).map(ProductBatchDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductBatchDto> findByStatus(BatchStatus status, Pageable pageable) {
        return batchRepository.findByCurrentStatus(status, pageable).map(ProductBatchDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductBatchDto> findByStatus(BatchStatus status, User requestingUser, Pageable pageable) {
        if (UserRole.FARMER.equals(requestingUser.getRole())) {
            return batchRepository.findByFarmerAndCurrentStatus(requestingUser, status, pageable)
                    .map(ProductBatchDto::from);
        }
        assertCanViewStatus(status, requestingUser);
        return batchRepository.findByCurrentStatus(status, pageable).map(ProductBatchDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public void assertCanAccessBatch(Long batchId, User requestingUser) {
        assertCanAccessBatch(getEntityById(batchId), requestingUser);
    }

    @Override
    public ProductBatchDto updateStatus(Long id, BatchStatus newStatus, User requestingUser) {
        ProductBatch batch = getEntityById(id);

        // Validate transition
        Set<BatchStatus> allowed = TRANSITIONS.getOrDefault(batch.getCurrentStatus(), EnumSet.noneOf(BatchStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new InvalidBatchStatusException(
                    "Cannot transition batch from " + batch.getCurrentStatus() + " to " + newStatus);
        }

        assertCanUpdateStatus(batch, newStatus, requestingUser);

        batch.setCurrentStatus(newStatus);
        return ProductBatchDto.from(batchRepository.save(batch));
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(BatchStatus status) {
        return batchRepository.countByCurrentStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByFarmer(User farmer) {
        return batchRepository.countByFarmer(farmer);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void assertRolePermittedForTransition(BatchStatus target, User user) {
        boolean permitted = switch (user.getRole()) {
            case FARMER           -> target == BatchStatus.IN_TRANSIT;
            case DISTRIBUTOR      -> target == BatchStatus.IN_WAREHOUSE || target == BatchStatus.IN_TRANSIT;
            case WAREHOUSE_MANAGER-> target == BatchStatus.QUALITY_INSPECTED || target == BatchStatus.IN_WAREHOUSE;
            case RETAILER         -> target == BatchStatus.AT_RETAILER || target == BatchStatus.SOLD;
            default               -> false;
        };
        if (!permitted) {
            throw new UnauthorizedAccessException(
                    "Your role (" + user.getRole() + ") cannot set status to " + target);
        }
    }

    private void assertCanAccessBatch(ProductBatch batch, User user) {
        if (UserRole.ADMIN.equals(user.getRole())) {
            return;
        }

        boolean isOwner = UserRole.FARMER.equals(user.getRole())
                && batch.getFarmer().getId().equals(user.getId());
        boolean isSupplyChainParticipant = switch (user.getRole()) {
            case DISTRIBUTOR -> batch.getCurrentStatus() == BatchStatus.IN_TRANSIT
                    || batch.getCurrentStatus() == BatchStatus.IN_WAREHOUSE;
            case WAREHOUSE_MANAGER -> batch.getCurrentStatus() == BatchStatus.IN_WAREHOUSE
                    || batch.getCurrentStatus() == BatchStatus.QUALITY_INSPECTED;
            case RETAILER -> batch.getCurrentStatus() == BatchStatus.QUALITY_INSPECTED
                    || batch.getCurrentStatus() == BatchStatus.AT_RETAILER
                    || batch.getCurrentStatus() == BatchStatus.SOLD;
            default -> false;
        };
        if (!isOwner && !isSupplyChainParticipant) {
            throw new UnauthorizedAccessException("You do not have permission to access this batch.");
        }
    }

    private void assertCanViewStatus(BatchStatus status, User user) {
        boolean permitted = UserRole.ADMIN.equals(user.getRole()) || switch (user.getRole()) {
            case DISTRIBUTOR -> status == BatchStatus.IN_TRANSIT || status == BatchStatus.IN_WAREHOUSE;
            case WAREHOUSE_MANAGER -> status == BatchStatus.IN_WAREHOUSE
                    || status == BatchStatus.QUALITY_INSPECTED;
            case RETAILER -> status == BatchStatus.QUALITY_INSPECTED
                    || status == BatchStatus.AT_RETAILER
                    || status == BatchStatus.SOLD;
            default -> false;
        };
        if (!permitted) {
            throw new UnauthorizedAccessException("Your role cannot view batches with status " + status + ".");
        }
    }

    private void assertCanUpdateStatus(ProductBatch batch, BatchStatus target, User user) {
        if (UserRole.ADMIN.equals(user.getRole())) {
            return;
        }
        if (UserRole.FARMER.equals(user.getRole())
                && !batch.getFarmer().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You can only update your own batches.");
        }
        assertCanAccessBatch(batch, user);
        assertRolePermittedForTransition(target, user);
    }
}
