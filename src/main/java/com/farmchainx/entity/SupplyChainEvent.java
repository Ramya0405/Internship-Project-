package com.farmchainx.entity;

import com.farmchainx.enums.EventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * An immutable supply-chain event recorded against a product batch.
 * Events are append-only; existing events must never be updated.
 */
@Entity
@Table(
    name = "supply_chain_events",
    indexes = {
        @Index(name = "idx_sce_batch",     columnList = "batch_id"),
        @Index(name = "idx_sce_timestamp", columnList = "event_timestamp"),
        @Index(name = "idx_sce_type",      columnList = "event_type")
    }
)
public class SupplyChainEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Batch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private ProductBatch batch;

    @NotNull(message = "Event type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private EventType eventType;

    @Column(length = 255)
    private String location;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column
    private Double temperature; // Celsius

    @Column
    private Double humidity;    // percentage

    @PrePersist
    protected void onCreate() {
        if (this.eventTimestamp == null) {
            this.eventTimestamp = LocalDateTime.now();
        }
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    public SupplyChainEvent() {}

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public ProductBatch getBatch() { return batch; }
    public void setBatch(ProductBatch batch) { this.batch = batch; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
}
