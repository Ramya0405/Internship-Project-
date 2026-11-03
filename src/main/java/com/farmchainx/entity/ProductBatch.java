package com.farmchainx.entity;

import com.farmchainx.enums.BatchStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A specific batch of an agricultural product harvested from a farm.
 * Each batch receives a globally unique tracking ID upon creation.
 */
@Entity
@Table(
    name = "product_batches",
    indexes = {
        @Index(name = "idx_batch_tracking_id", columnList = "tracking_id", unique = true),
        @Index(name = "idx_batch_farm",        columnList = "farm_id"),
        @Index(name = "idx_batch_farmer",      columnList = "farmer_id"),
        @Index(name = "idx_batch_status",      columnList = "current_status")
    }
)
public class ProductBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tracking ID is required")
    @Column(name = "tracking_id", nullable = false, unique = true, length = 30)
    private String trackingId;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Farm is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @NotNull(message = "Farmer is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Double quantity; // in kilograms

    @NotNull(message = "Harvest date is required")
    @Column(name = "harvest_date", nullable = false)
    private LocalDate harvestDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 30)
    private BatchStatus currentStatus = BatchStatus.CREATED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    public ProductBatch() {}

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }

    public User getFarmer() { return farmer; }
    public void setFarmer(User farmer) { this.farmer = farmer; }

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
