package com.farmchainx.dto;

import com.farmchainx.entity.ProductBatch;
import com.farmchainx.enums.BatchStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for ProductBatch – creation request and API response.
 */
public class ProductBatchDto {

    private Long id;
    private String trackingId;

    @NotNull(message = "Product ID is required")
    private Long productId;
    private String productName;
    private String productCategory;

    @NotNull(message = "Farm ID is required")
    private Long farmId;
    private String farmName;
    private String farmLocation;

    private Long farmerId;
    private String farmerName;

    @Positive(message = "Quantity must be positive")
    @NotNull(message = "Quantity is required")
    private Double quantity;

    @NotNull(message = "Harvest date is required")
    private LocalDate harvestDate;

    private LocalDate expiryDate;

    private BatchStatus currentStatus;
    private LocalDateTime createdAt;

    public ProductBatchDto() {}

    public static ProductBatchDto from(ProductBatch batch) {
        ProductBatchDto dto = new ProductBatchDto();
        dto.id              = batch.getId();
        dto.trackingId      = batch.getTrackingId();
        dto.productId       = batch.getProduct() != null ? batch.getProduct().getId()          : null;
        dto.productName     = batch.getProduct() != null ? batch.getProduct().getProductName() : null;
        dto.productCategory = batch.getProduct() != null ? batch.getProduct().getCategory()    : null;
        dto.farmId          = batch.getFarm()    != null ? batch.getFarm().getId()             : null;
        dto.farmName        = batch.getFarm()    != null ? batch.getFarm().getFarmName()       : null;
        dto.farmLocation    = batch.getFarm()    != null ? batch.getFarm().getLocation()       : null;
        dto.farmerId        = batch.getFarmer()  != null ? batch.getFarmer().getId()           : null;
        dto.farmerName      = batch.getFarmer()  != null ? batch.getFarmer().getFullName()     : null;
        dto.quantity        = batch.getQuantity();
        dto.harvestDate     = batch.getHarvestDate();
        dto.expiryDate      = batch.getExpiryDate();
        dto.currentStatus   = batch.getCurrentStatus();
        dto.createdAt       = batch.getCreatedAt();
        return dto;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }

    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

    public String getFarmLocation() { return farmLocation; }
    public void setFarmLocation(String farmLocation) { this.farmLocation = farmLocation; }

    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public LocalDate getHarvestDate() { return harvestDate; }
    public void setHarvestDate(LocalDate harvestDate) { this.harvestDate = harvestDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public BatchStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(BatchStatus currentStatus) { this.currentStatus = currentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
