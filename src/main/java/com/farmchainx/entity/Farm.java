package com.farmchainx.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Represents a farm registered by a FARMER user.
 */
@Entity
@Table(
    name = "farms",
    indexes = {
        @Index(name = "idx_farm_owner", columnList = "owner_id")
    }
)
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Farm name is required")
    @Size(min = 2, max = 150)
    @Column(name = "farm_name", nullable = false, length = 150)
    private String farmName;

    @NotBlank(message = "Location is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "farm_size")
    private Double farmSize; // in acres

    @Size(max = 200)
    @Column(length = 200)
    private String certification;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    public Farm() {}

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Double getFarmSize() { return farmSize; }
    public void setFarmSize(Double farmSize) { this.farmSize = farmSize; }

    public String getCertification() { return certification; }
    public void setCertification(String certification) { this.certification = certification; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
