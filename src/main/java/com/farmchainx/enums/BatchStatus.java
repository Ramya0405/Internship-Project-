package com.farmchainx.enums;

/**
 * Lifecycle status of an agricultural product batch as it moves through the supply chain.
 */
public enum BatchStatus {
    CREATED,
    IN_TRANSIT,
    IN_WAREHOUSE,
    QUALITY_INSPECTED,
    AT_RETAILER,
    SOLD,
    RECALLED,
    EXPIRED
}
