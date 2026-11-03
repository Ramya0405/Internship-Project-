package com.farmchainx.restcontroller;

import com.farmchainx.dto.ProductBatchDto;
import com.farmchainx.enums.BatchStatus;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.ProductBatchService;
import com.farmchainx.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batches")
public class BatchRestController {

    private final ProductBatchService batchService;

    public BatchRestController(ProductBatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FARMER')")
    public ResponseEntity<ApiResponse<ProductBatchDto>> create(
            @Valid @RequestBody ProductBatchDto dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        ProductBatchDto created = batchService.create(dto, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Batch created. Tracking ID: " + created.getTrackingId(), created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FARMER','DISTRIBUTOR','WAREHOUSE_MANAGER','RETAILER')")
    public ResponseEntity<ApiResponse<Page<ProductBatchDto>>> getAll(
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "10") int    size,
            @RequestParam(required = false)    String status,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Page<ProductBatchDto> batches;
        String role = principal.getRole();

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if ("ADMIN".equals(role)) {
            batches = status == null || status.isBlank()
                    ? batchService.findAll(pageable)
                    : batchService.findByStatus(BatchStatus.valueOf(status.toUpperCase()), pageable);
        } else if ("FARMER".equals(role)) {
            batches = status != null && !status.isBlank()
                    ? batchService.findByStatus(BatchStatus.valueOf(status.toUpperCase()),
                            principal.getUser(), pageable)
                    : batchService.findByFarmer(principal.getUser(), pageable);
        } else if (status != null && !status.isBlank()) {
            batches = batchService.findByStatus(BatchStatus.valueOf(status.toUpperCase()),
                    principal.getUser(), pageable);
        } else if ("DISTRIBUTOR".equals(role)) {
            batches = batchService.findByStatus(BatchStatus.IN_TRANSIT, principal.getUser(), pageable);
        } else if ("WAREHOUSE_MANAGER".equals(role)) {
            batches = batchService.findByStatus(BatchStatus.IN_WAREHOUSE, principal.getUser(), pageable);
        } else if ("RETAILER".equals(role)) {
            batches = batchService.findByStatus(BatchStatus.AT_RETAILER, principal.getUser(), pageable);
        } else {
            throw new IllegalStateException("Unsupported role for batch listing: " + role);
        }
        return ResponseEntity.ok(ApiResponse.success(batches));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER','DISTRIBUTOR','WAREHOUSE_MANAGER','RETAILER')")
    public ResponseEntity<ApiResponse<ProductBatchDto>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(batchService.findById(id, principal.getUser())));
    }

    @GetMapping("/track/{trackingId}")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER','DISTRIBUTOR','WAREHOUSE_MANAGER','RETAILER')")
    public ResponseEntity<ApiResponse<ProductBatchDto>> getByTrackingId(
            @PathVariable String trackingId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                batchService.findByTrackingId(trackingId, principal.getUser())));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER','DISTRIBUTOR','WAREHOUSE_MANAGER','RETAILER')")
    public ResponseEntity<ApiResponse<ProductBatchDto>> updateStatus(
            @PathVariable Long id,
            @RequestParam BatchStatus status,
            @AuthenticationPrincipal CustomUserDetails principal) {
        ProductBatchDto updated = batchService.updateStatus(id, status, principal.getUser());
        return ResponseEntity.ok(ApiResponse.success("Status updated to " + status, updated));
    }
}
