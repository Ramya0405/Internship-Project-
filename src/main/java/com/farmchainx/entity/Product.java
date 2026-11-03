package com.farmchainx.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Master catalogue of agricultural product types (e.g. "Tomato", "Rice").
 * Created by ADMIN or FARMER users.
 */
@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_product_category", columnList = "category")
    }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 150)
    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @NotBlank(message = "Category is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String category;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @Column(name = "product_image")
    private String productImage; // relative path to uploaded image

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    public Product() {}

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
