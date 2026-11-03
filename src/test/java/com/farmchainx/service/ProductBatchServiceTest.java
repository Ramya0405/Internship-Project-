package com.farmchainx.service;

import com.farmchainx.TestEntityHelper;
import com.farmchainx.dto.ProductBatchDto;
import com.farmchainx.entity.Farm;
import com.farmchainx.entity.Product;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.User;
import com.farmchainx.enums.BatchStatus;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.InvalidBatchStatusException;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.repository.ProductBatchRepository;
import com.farmchainx.service.impl.ProductBatchServiceImpl;
import com.farmchainx.util.TrackingIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductBatchServiceTest {

    @Mock
    private ProductBatchRepository batchRepository;

    @Mock
    private ProductService productService;

    @Mock
    private FarmService farmService;

    private ProductBatchServiceImpl batchService;

    private User farmer;
    private User distributor;
    private User admin;
    private Farm farm;
    private Product product;
    private ProductBatch batch;

    @BeforeEach
    void setUp() {
        farmer = new User();
        TestEntityHelper.withId(farmer, 10L);
        farmer.setFullName("Farmer Joe");
        farmer.setRole(UserRole.FARMER);

        distributor = new User();
        TestEntityHelper.withId(distributor, 20L);
        distributor.setFullName("Distributor Dave");
        distributor.setRole(UserRole.DISTRIBUTOR);

        admin = new User();
        TestEntityHelper.withId(admin, 1L);
        admin.setFullName("Admin Alice");
        admin.setRole(UserRole.ADMIN);

        farm = new Farm();
        TestEntityHelper.withId(farm, 100L);
        farm.setFarmName("Sunny Acre Farm");
        farm.setOwner(farmer);

        product = new Product();
        TestEntityHelper.withId(product, 200L);
        product.setProductName("Organic Tomatoes");
        product.setCategory("Vegetable");

        batch = new ProductBatch();
        TestEntityHelper.withId(batch, 500L);
        batch.setTrackingId("FCX-20240501-A1B2C3D4");
        batch.setProduct(product);
        batch.setFarm(farm);
        batch.setFarmer(farmer);
        batch.setQuantity(250.0);
        batch.setHarvestDate(LocalDate.now().minusDays(2));
        batch.setExpiryDate(LocalDate.now().plusDays(10));
        batch.setCurrentStatus(BatchStatus.CREATED);

        batchService = new ProductBatchServiceImpl(
                batchRepository, productService, farmService, new TrackingIdGenerator());
    }

    @Test
    @DisplayName("Create product batch generates unique tracking ID")
    void create_success() {
        ProductBatchDto dto = new ProductBatchDto();
        dto.setProductId(200L);
        dto.setFarmId(100L);
        dto.setQuantity(250.0);
        dto.setHarvestDate(LocalDate.now());

        when(batchRepository.existsByTrackingId(any())).thenReturn(false);
        when(productService.getEntityById(200L)).thenReturn(product);
        when(farmService.getEntityById(100L)).thenReturn(farm);
        when(batchRepository.save(any(ProductBatch.class))).thenAnswer(i -> {
            ProductBatch b = i.getArgument(0);
            TestEntityHelper.withId(b, 500L);
            return b;
        });

        ProductBatchDto created = batchService.create(dto, farmer);

        assertThat(created).isNotNull();
        assertThat(created.getTrackingId()).matches("FCX-\\d{8}-[A-F0-9]{8}");
        assertThat(created.getCurrentStatus()).isEqualTo(BatchStatus.CREATED);
        verify(batchRepository).save(any(ProductBatch.class));
    }

    @Test
    @DisplayName("Farmer cannot create a batch using another farmer's farm")
    void create_farmNotOwnedByFarmer_throwsException() {
        User otherFarmer = new User();
        TestEntityHelper.withId(otherFarmer, 11L);
        otherFarmer.setRole(UserRole.FARMER);
        farm.setOwner(otherFarmer);

        ProductBatchDto dto = new ProductBatchDto();
        dto.setProductId(200L);
        dto.setFarmId(100L);
        dto.setQuantity(250.0);
        dto.setHarvestDate(LocalDate.now());

        when(batchRepository.existsByTrackingId(any())).thenReturn(false);
        when(productService.getEntityById(200L)).thenReturn(product);
        when(farmService.getEntityById(100L)).thenReturn(farm);

        assertThatThrownBy(() -> batchService.create(dto, farmer))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("your own farms");
        verify(batchRepository, never()).save(any(ProductBatch.class));
    }

    @Test
    @DisplayName("Find batch by ID")
    void findById_success() {
        when(batchRepository.findById(500L)).thenReturn(Optional.of(batch));

        ProductBatchDto result = batchService.findById(500L);

        assertThat(result).isNotNull();
        assertThat(result.getTrackingId()).isEqualTo("FCX-20240501-A1B2C3D4");
        assertThat(result.getProductName()).isEqualTo("Organic Tomatoes");
    }

    @Test
    @DisplayName("Find batch by tracking ID")
    void findByTrackingId_success() {
        when(batchRepository.findByTrackingId("FCX-20240501-A1B2C3D4")).thenReturn(Optional.of(batch));

        ProductBatchDto result = batchService.findByTrackingId("FCX-20240501-A1B2C3D4");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(500L);
    }

    @Test
    @DisplayName("Farmer successfully updates status from CREATED to IN_TRANSIT")
    void updateStatus_farmerValidTransition() {
        when(batchRepository.findById(500L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any(ProductBatch.class))).thenAnswer(i -> i.getArgument(0));

        ProductBatchDto updated = batchService.updateStatus(500L, BatchStatus.IN_TRANSIT, farmer);

        assertThat(updated.getCurrentStatus()).isEqualTo(BatchStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("Farmer cannot update another farmer's batch")
    void updateStatus_farmerDoesNotOwnBatch_throwsException() {
        User otherFarmer = new User();
        TestEntityHelper.withId(otherFarmer, 11L);
        otherFarmer.setRole(UserRole.FARMER);
        batch.setFarmer(otherFarmer);
        when(batchRepository.findById(500L)).thenReturn(Optional.of(batch));

        assertThatThrownBy(() -> batchService.updateStatus(500L, BatchStatus.IN_TRANSIT, farmer))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("your own batches");
        verify(batchRepository, never()).save(any(ProductBatch.class));
    }

    @Test
    @DisplayName("Distributor successfully updates status from IN_TRANSIT to IN_WAREHOUSE")
    void updateStatus_distributorValidTransition() {
        batch.setCurrentStatus(BatchStatus.IN_TRANSIT);
        when(batchRepository.findById(500L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any(ProductBatch.class))).thenAnswer(i -> i.getArgument(0));

        ProductBatchDto updated = batchService.updateStatus(500L, BatchStatus.IN_WAREHOUSE, distributor);

        assertThat(updated.getCurrentStatus()).isEqualTo(BatchStatus.IN_WAREHOUSE);
    }

    @Test
    @DisplayName("Invalid status transition throws InvalidBatchStatusException")
    void updateStatus_invalidTransition_throwsException() {
        // CREATED -> SOLD is not an allowed direct transition
        when(batchRepository.findById(500L)).thenReturn(Optional.of(batch));

        assertThatThrownBy(() -> batchService.updateStatus(500L, BatchStatus.SOLD, admin))
                .isInstanceOf(InvalidBatchStatusException.class)
                .hasMessageContaining("Cannot transition batch from CREATED to SOLD");
    }

    @Test
    @DisplayName("Unauthorized role transition throws UnauthorizedAccessException")
    void updateStatus_unauthorizedRole_throwsException() {
        // Farmer cannot transition to AT_RETAILER
        batch.setCurrentStatus(BatchStatus.QUALITY_INSPECTED);
        when(batchRepository.findById(500L)).thenReturn(Optional.of(batch));

        assertThatThrownBy(() -> batchService.updateStatus(500L, BatchStatus.AT_RETAILER, farmer))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Your role (FARMER) cannot set status to AT_RETAILER");
    }

    @Test
    @DisplayName("Admin can execute any valid transition without role restrictions")
    void updateStatus_adminCanRecall() {
        // Any status (except final ones) -> RECALLED is valid
        when(batchRepository.findById(500L)).thenReturn(Optional.of(batch));
        when(batchRepository.save(any(ProductBatch.class))).thenAnswer(i -> i.getArgument(0));

        ProductBatchDto updated = batchService.updateStatus(500L, BatchStatus.RECALLED, admin);

        assertThat(updated.getCurrentStatus()).isEqualTo(BatchStatus.RECALLED);
    }
}
