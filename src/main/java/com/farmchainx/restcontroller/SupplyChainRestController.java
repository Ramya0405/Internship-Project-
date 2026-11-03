package com.farmchainx.restcontroller;

import com.farmchainx.dto.SupplyChainEventDto;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.SupplyChainEventService;
import com.farmchainx.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
public class SupplyChainRestController {

    private final SupplyChainEventService eventService;

    public SupplyChainRestController(SupplyChainEventService eventService) {
        this.eventService = eventService;
    }

    /** Add a supply-chain event to a batch. */
    @PostMapping("/{batchId}/events")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER','DISTRIBUTOR','WAREHOUSE_MANAGER','RETAILER')")
    public ResponseEntity<ApiResponse<SupplyChainEventDto>> addEvent(
            @PathVariable Long batchId,
            @Valid @RequestBody SupplyChainEventDto dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        dto.setBatchId(batchId);
        SupplyChainEventDto created = eventService.addEvent(dto, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event recorded", created));
    }

    /** Retrieve the full supply-chain history for a batch. */
    @GetMapping("/{batchId}/history")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER','DISTRIBUTOR','WAREHOUSE_MANAGER','RETAILER')")
    public ResponseEntity<ApiResponse<List<SupplyChainEventDto>>> getHistory(
            @PathVariable Long batchId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.success(eventService.getHistoryForBatch(batchId, principal.getUser())));
    }
}
