package com.farmchainx.dto;

import com.farmchainx.entity.SupplyChainEvent;
import com.farmchainx.enums.EventType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO for supply-chain events – creation request and read response.
 */
public class SupplyChainEventDto {

    private Long id;

    @NotNull(message = "Batch ID is required")
    private Long batchId;
    private String trackingId;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    private String location;
    private String description;
    private Long updatedById;
    private String updatedByName;
    private LocalDateTime eventTimestamp;
    private Double temperature;
    private Double humidity;

    public SupplyChainEventDto() {}

    public static SupplyChainEventDto from(SupplyChainEvent e) {
        SupplyChainEventDto dto = new SupplyChainEventDto();
        dto.id              = e.getId();
        dto.batchId         = e.getBatch()     != null ? e.getBatch().getId()                   : null;
        dto.trackingId      = e.getBatch()     != null ? e.getBatch().getTrackingId()            : null;
        dto.eventType       = e.getEventType();
        dto.location        = e.getLocation();
        dto.description     = e.getDescription();
        dto.updatedById     = e.getUpdatedBy() != null ? e.getUpdatedBy().getId()               : null;
        dto.updatedByName   = e.getUpdatedBy() != null ? e.getUpdatedBy().getFullName()         : null;
        dto.eventTimestamp  = e.getEventTimestamp();
        dto.temperature     = e.getTemperature();
        dto.humidity        = e.getHumidity();
        return dto;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getUpdatedById() { return updatedById; }
    public void setUpdatedById(Long updatedById) { this.updatedById = updatedById; }

    public String getUpdatedByName() { return updatedByName; }
    public void setUpdatedByName(String updatedByName) { this.updatedByName = updatedByName; }

    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
}
