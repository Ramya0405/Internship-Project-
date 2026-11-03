package com.farmchainx.restcontroller;

import com.farmchainx.dto.FarmDto;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.FarmService;
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
@RequestMapping("/api/farms")
public class FarmRestController {

    private final FarmService farmService;

    public FarmRestController(FarmService farmService) {
        this.farmService = farmService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FARMER')")
    public ResponseEntity<ApiResponse<FarmDto>> create(
            @Valid @RequestBody FarmDto dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        FarmDto created = farmService.create(dto, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Farm registered", created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FARMER')")
    public ResponseEntity<ApiResponse<Page<FarmDto>>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Page<FarmDto> farms;
        if ("ADMIN".equals(principal.getRole())) {
            farms = farmService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            farms = farmService.findByOwner(principal.getUser(),
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }
        return ResponseEntity.ok(ApiResponse.success(farms));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER')")
    public ResponseEntity<ApiResponse<FarmDto>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(farmService.findById(id, principal.getUser())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER')")
    public ResponseEntity<ApiResponse<FarmDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody FarmDto dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        FarmDto updated = farmService.update(id, dto, principal.getUser());
        return ResponseEntity.ok(ApiResponse.success("Farm updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        farmService.delete(id, principal.getUser());
        return ResponseEntity.ok(ApiResponse.success("Farm deleted"));
    }
}
