package com.farmchainx.restcontroller;

import com.farmchainx.dto.ProductDto;
import com.farmchainx.service.ProductService;
import com.farmchainx.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    private final ProductService productService;

    public ProductRestController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FARMER')")
    public ResponseEntity<ApiResponse<ProductDto>> create(
            @Valid @RequestBody ProductDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", productService.create(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getAll(
            @RequestParam(defaultValue = "0")   int    page,
            @RequestParam(defaultValue = "10")  int    size,
            @RequestParam(defaultValue = "")    String search) {
        Page<ProductDto> products = search.isBlank()
                ? productService.findAll(PageRequest.of(page, size, Sort.by("productName")))
                : productService.search(search, PageRequest.of(page, size, Sort.by("productName")));
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FARMER')")
    public ResponseEntity<ApiResponse<ProductDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Product updated", productService.update(id, dto)));
    }
}
