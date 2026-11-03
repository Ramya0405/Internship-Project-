package com.farmchainx.dto;

import com.farmchainx.entity.Transportation;
import com.farmchainx.enums.TransportationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO for a transportation record.
 */
public class TransportationDto {

    private Long id;

    @NotNull(message = "Batch ID is required")
    private Long batchId;
    private String trackingId;
    private String productName;

    private Long distributorId;
    private String distributorName;

    @NotBlank(message = "Source location is required")
    private String sourceLocation;

    @NotBlank(message = "Destination location is required")
    private String destinationLocation;

    private LocalDateTime departureDate;
    private LocalDateTime estimatedArrival;
    private LocalDateTime actualArrival;

    private TransportationStatus transportationStatus;

    private String vehicleNumber;
    private String notes;

    public TransportationDto() {}

    public static TransportationDto from(Transportation t) {
        TransportationDto dto = new TransportationDto();
        dto.id                  = t.getId();
        dto.batchId             = t.getBatch()       != null ? t.getBatch().getId()               : null;
        dto.trackingId          = t.getBatch()       != null ? t.getBatch().getTrackingId()        : null;
        dto.productName         = t.getBatch() != null && t.getBatch().getProduct() != null
                ? t.getBatch().getProduct().getProductName()
                : null;
        dto.distributorId       = t.getDistributor() != null ? t.getDistributor().getId()          : null;
        dto.distributorName     = t.getDistributor() != null ? t.getDistributor().getFullName()    : null;
        dto.sourceLocation      = t.getSourceLocation();
        dto.destinationLocation = t.getDestinationLocation();
        dto.departureDate       = t.getDepartureDate();
        dto.estimatedArrival    = t.getEstimatedArrival();
        dto.actualArrival       = t.getActualArrival();
        dto.transportationStatus = t.getTransportationStatus();
        dto.vehicleNumber       = t.getVehicleNumber();
        dto.notes               = t.getNotes();
        return dto;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Long getDistributorId() { return distributorId; }
    public void setDistributorId(Long distributorId) { this.distributorId = distributorId; }

    public String getDistributorName() { return distributorName; }
    public void setDistributorName(String distributorName) { this.distributorName = distributorName; }

    public String getSourceLocation() { return sourceLocation; }
    public void setSourceLocation(String sourceLocation) { this.sourceLocation = sourceLocation; }

    public String getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; }

    public LocalDateTime getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDateTime departureDate) { this.departureDate = departureDate; }

    public LocalDateTime getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(LocalDateTime estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public LocalDateTime getActualArrival() { return actualArrival; }
    public void setActualArrival(LocalDateTime actualArrival) { this.actualArrival = actualArrival; }

    public TransportationStatus getTransportationStatus() { return transportationStatus; }
    public void setTransportationStatus(TransportationStatus transportationStatus) { this.transportationStatus = transportationStatus; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
