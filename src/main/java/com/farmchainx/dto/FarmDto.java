package com.farmchainx.dto;

import com.farmchainx.entity.Farm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO for Farm – used for both creation/update requests and API responses.
 */
public class FarmDto {

    private Long id;

    @NotBlank(message = "Farm name is required")
    @Size(min = 2, max = 150)
    private String farmName;

    @NotBlank(message = "Location is required")
    @Size(max = 255)
    private String location;

    private Long ownerId;
    private String ownerName;

    private Double farmSize;

    @Size(max = 200)
    private String certification;

    private LocalDateTime createdAt;

    public FarmDto() {}

    public static FarmDto from(Farm farm) {
        FarmDto dto = new FarmDto();
        dto.id            = farm.getId();
        dto.farmName      = farm.getFarmName();
        dto.location      = farm.getLocation();
        dto.ownerId       = farm.getOwner() != null ? farm.getOwner().getId()       : null;
        dto.ownerName     = farm.getOwner() != null ? farm.getOwner().getFullName() : null;
        dto.farmSize      = farm.getFarmSize();
        dto.certification = farm.getCertification();
        dto.createdAt     = farm.getCreatedAt();
        return dto;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public Double getFarmSize() { return farmSize; }
    public void setFarmSize(Double farmSize) { this.farmSize = farmSize; }

    public String getCertification() { return certification; }
    public void setCertification(String certification) { this.certification = certification; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
