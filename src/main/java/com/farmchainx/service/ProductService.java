package com.farmchainx.service;

import com.farmchainx.dto.ProductDto;
import com.farmchainx.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductDto create(ProductDto dto);

    ProductDto findById(Long id);

    Product getEntityById(Long id);

    Page<ProductDto> findAll(Pageable pageable);

    Page<ProductDto> search(String query, Pageable pageable);

    ProductDto update(Long id, ProductDto dto);

    void delete(Long id);
}
