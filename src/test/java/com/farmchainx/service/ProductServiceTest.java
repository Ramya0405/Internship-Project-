package com.farmchainx.service;

import com.farmchainx.TestEntityHelper;
import com.farmchainx.dto.ProductDto;
import com.farmchainx.entity.Product;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.repository.ProductRepository;
import com.farmchainx.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        TestEntityHelper.withId(product, 1L);
        product.setProductName("Basmati Rice");
        product.setCategory("Grain");
        product.setDescription("Premium aromatic long-grain rice");
    }

    @Test
    @DisplayName("Create product in catalogue")
    void create_success() {
        ProductDto dto = new ProductDto();
        dto.setProductName("Basmati Rice");
        dto.setCategory("Grain");
        dto.setDescription("Premium aromatic long-grain rice");

        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            TestEntityHelper.withId(p, 1L);
            return p;
        });

        ProductDto created = productService.create(dto);

        assertThat(created).isNotNull();
        assertThat(created.getProductName()).isEqualTo("Basmati Rice");
        assertThat(created.getCategory()).isEqualTo("Grain");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Find product by ID")
    void findById_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDto result = productService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getProductName()).isEqualTo("Basmati Rice");
    }

    @Test
    @DisplayName("Find product by invalid ID throws ResourceNotFoundException")
    void findById_notFound_throwsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Search products by keyword")
    void search_returnsMatchingProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByProductNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                eq("Rice"), eq("Rice"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<ProductDto> results = productService.search("Rice", pageable);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getProductName()).isEqualTo("Basmati Rice");
    }

    @Test
    @DisplayName("Update existing product")
    void update_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductDto updateDto = new ProductDto();
        updateDto.setProductName("Super Basmati Rice");
        updateDto.setCategory("Grain");
        updateDto.setDescription("Updated description");

        ProductDto updated = productService.update(1L, updateDto);

        assertThat(updated.getProductName()).isEqualTo("Super Basmati Rice");
    }

    @Test
    @DisplayName("Delete product by ID")
    void delete_success() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deleting a missing product throws without deleting")
    void delete_notFound_throwsException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }
}
