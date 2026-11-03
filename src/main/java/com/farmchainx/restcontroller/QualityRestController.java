package com.farmchainx.restcontroller;

import com.farmchainx.dto.QualityInspectionDto;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.QualityInspectionService;
import com.farmchainx.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality-inspections")
public class QualityRestController {

    private final QualityInspectionService inspectionService;

    public QualityRestController(QualityInspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<QualityInspectionDto>> create(
            @Valid @RequestBody QualityInspectionDto dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        QualityInspectionDto created = inspectionService.create(dto, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quality inspection recorded", created));
    }

    @GetMapping("/{batchId}")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER','DISTRIBUTOR','WAREHOUSE_MANAGER','RETAILER')")
    public ResponseEntity<ApiResponse<List<QualityInspectionDto>>> getByBatch(
            @PathVariable Long batchId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(
                ApiResponse.success(inspectionService.findByBatch(batchId, principal.getUser())));
    }
}
