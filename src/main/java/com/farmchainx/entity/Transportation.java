package com.farmchainx.entity;

import com.farmchainx.enums.TransportationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Transportation record for moving a product batch between locations.
 */
@Entity
@Table(
    name = "transportation",
    indexes = {
        @Index(name = "idx_transport_batch",       columnList = "batch_id"),
        @Index(name = "idx_transport_distributor", columnList = "distributor_id"),
        @Index(name = "idx_transport_status",      columnList = "transportation_status")
    }
)
public class Transportation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Batch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private ProductBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distributor_id")
    private User distributor;

    @NotBlank(message = "Source location is required")
    @Column(name = "source_location", nullable = false, length = 255)
    private String sourceLocation;

    @NotBlank(message = "Destination location is required")
    @Column(name = "destination_location", nullable = false, length = 255)
    private String destinationLocation;

    @Column(name = "departure_date")
    private LocalDateTime departureDate;

    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;

    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;

    @Enumerated(EnumType.STRING)
    @Column(name = "transportation_status", nullable = false, length = 20)
    private TransportationStatus transportationStatus = TransportationStatus.SCHEDULED;

    @Column(name = "vehicle_number", length = 30)
    private String vehicleNumber;

    @Column(length = 500)
    private String notes;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Transportation() {}

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public ProductBatch getBatch() { return batch; }
    public void setBatch(ProductBatch batch) { this.batch = batch; }

    public User getDistributor() { return distributor; }
    public void setDistributor(User distributor) { this.distributor = distributor; }

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
