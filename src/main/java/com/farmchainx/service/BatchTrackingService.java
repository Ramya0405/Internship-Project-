package com.farmchainx.service;

import com.farmchainx.dto.BatchTrackingResponse;

/**
 * Assembles the full product journey for a given tracking ID.
 * This is the service called by the consumer-facing public tracking API.
 */
public interface BatchTrackingService {

    BatchTrackingResponse getFullJourney(String trackingId);
}
