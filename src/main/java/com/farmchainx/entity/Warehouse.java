package com.farmchainx.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Physical warehouse where batches are stored before onward distribution.
 */
@Entity
@Table(
    name = "warehouses",
    indexes = {
        @Index(name = "idx_warehouse_manager", columnList = "manager_id")
    }
)
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Warehouse name is required")
    @Size(min = 2, max = 150)
    @Column(name = "warehouse_name", nullable = false, length = 150)
    private String warehouseName;

    @NotBlank(message = "Location is required")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(name = "capacity_tons")
    private Double capacityTons;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Warehouse() {}

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }

    public Double getCapacityTons() { return capacityTons; }
    public void setCapacityTons(Double capacityTons) { this.capacityTons = capacityTons; }
}
