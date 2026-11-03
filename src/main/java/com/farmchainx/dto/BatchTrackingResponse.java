package com.farmchainx.dto;

import com.farmchainx.enums.BatchStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Data intentionally approved for display through the public tracking API.
 */
public class BatchTrackingResponse {

    // Batch basics
    private String trackingId;
    private BatchStatus currentStatus;

    // Product
    private String productName;
    private String productCategory;

    // Batch metadata
    private Double quantity;
    private LocalDate harvestDate;
    private LocalDate expiryDate;

    // Journey
    private List<PublicTrackingEventDto> events;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }

    public BatchStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(BatchStatus currentStatus) { this.currentStatus = currentStatus; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public LocalDate getHarvestDate() { return harvestDate; }
    public void setHarvestDate(LocalDate harvestDate) { this.harvestDate = harvestDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public List<PublicTrackingEventDto> getEvents() { return events; }
    public void setEvents(List<PublicTrackingEventDto> events) { this.events = events; }
}
