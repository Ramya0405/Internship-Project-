package com.farmchainx.enums;

/**
 * Types of supply-chain events that can be recorded against a product batch.
 */
public enum EventType {
    HARVESTED,
    PACKED,
    DISPATCHED,
    IN_TRANSIT,
    ARRIVED_AT_WAREHOUSE,
    STORED,
    QUALITY_CHECK,
    DISPATCHED_TO_RETAILER,
    ARRIVED_AT_RETAILER,
    SOLD,
    RECALLED,
    TEMPERATURE_ALERT,
    HUMIDITY_ALERT,
    DELAY_REPORTED,
    CUSTOMS_CLEARED,
    DAMAGED
}
