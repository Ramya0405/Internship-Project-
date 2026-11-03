package com.farmchainx.dto;

import com.farmchainx.entity.Warehouse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for warehouse records.
 */
public class WarehouseDto {

    private Long id;

    @NotBlank(message = "Warehouse name is required")
    @Size(min = 2, max = 150)
    private String warehouseName;

    @NotBlank(message = "Location is required")
    @Size(max = 255)
    private String location;

    private Long managerId;
    private String managerName;
    private Double capacityTons;

    public WarehouseDto() {}

    public static WarehouseDto from(Warehouse w) {
        WarehouseDto dto = new WarehouseDto();
        dto.id           = w.getId();
        dto.warehouseName= w.getWarehouseName();
        dto.location     = w.getLocation();
        dto.managerId    = w.getManager() != null ? w.getManager().getId()       : null;
        dto.managerName  = w.getManager() != null ? w.getManager().getFullName() : null;
        dto.capacityTons = w.getCapacityTons();
        return dto;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public Double getCapacityTons() { return capacityTons; }
    public void setCapacityTons(Double capacityTons) { this.capacityTons = capacityTons; }
}
