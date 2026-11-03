package com.farmchainx.restcontroller;

import com.farmchainx.dto.BatchTrackingResponse;
import com.farmchainx.service.BatchTrackingService;
import com.farmchainx.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public REST endpoints – no authentication required.
 * Used by the consumer product-tracking page.
 */
@RestController
@RequestMapping("/api/public")
public class PublicRestController {

    private final BatchTrackingService trackingService;

    public PublicRestController(BatchTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    /**
     * Returns the complete product journey for a given tracking ID.
     * This is the single entry point for consumer product verification.
     */
    @GetMapping("/track/{trackingId}")
    public ResponseEntity<ApiResponse<BatchTrackingResponse>> track(
            @PathVariable String trackingId) {
        BatchTrackingResponse journey = trackingService.getFullJourney(trackingId.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(journey));
    }
}
