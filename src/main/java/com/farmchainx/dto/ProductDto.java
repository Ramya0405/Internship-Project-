package com.farmchainx.dto;

import com.farmchainx.entity.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO for the product catalogue.
 */
public class ProductDto {

    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 150)
    private String productName;

    @NotBlank(message = "Category is required")
    @Size(max = 100)
    private String category;

    @Size(max = 500)
    private String description;

    private String productImage;
    private LocalDateTime createdAt;

    public ProductDto() {}

    public static ProductDto from(Product product) {
        ProductDto dto = new ProductDto();
        dto.id           = product.getId();
        dto.productName  = product.getProductName();
        dto.category     = product.getCategory();
        dto.description  = product.getDescription();
        dto.productImage = product.getProductImage();
        dto.createdAt    = product.getCreatedAt();
        return dto;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
