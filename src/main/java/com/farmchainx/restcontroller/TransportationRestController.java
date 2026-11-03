package com.farmchainx.restcontroller;

import com.farmchainx.dto.TransportationDto;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.TransportationService;
import com.farmchainx.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transportation")
public class TransportationRestController {

    private final TransportationService transportationService;

    public TransportationRestController(TransportationService transportationService) {
        this.transportationService = transportationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISTRIBUTOR','ADMIN')")
    public ResponseEntity<ApiResponse<TransportationDto>> create(
            @Valid @RequestBody TransportationDto dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        TransportationDto created = transportationService.create(dto, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transportation created", created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR','ADMIN')")
    public ResponseEntity<ApiResponse<TransportationDto>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(transportationService.findById(id, principal.getUser())));
    }

    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER','DISTRIBUTOR','WAREHOUSE_MANAGER','RETAILER')")
    public ResponseEntity<ApiResponse<List<TransportationDto>>> getByBatch(
            @PathVariable Long batchId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.success(transportationService.findByBatch(batchId, principal.getUser())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR','ADMIN')")
    public ResponseEntity<ApiResponse<TransportationDto>> update(
            @PathVariable Long id,
            @RequestBody TransportationDto dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        TransportationDto updated = transportationService.update(id, dto, principal.getUser());
        return ResponseEntity.ok(ApiResponse.success("Transportation updated", updated));
    }
}
