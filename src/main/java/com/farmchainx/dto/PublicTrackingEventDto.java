package com.farmchainx.dto;

import com.farmchainx.enums.EventType;

import java.time.LocalDateTime;

public class PublicTrackingEventDto {

    private EventType eventType;
    private LocalDateTime eventTimestamp;

    public PublicTrackingEventDto(EventType eventType, LocalDateTime eventTimestamp) {
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }
}
