package com.farmchainx.service.impl;

import com.farmchainx.dto.ProductDto;
import com.farmchainx.entity.Product;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.repository.ProductRepository;
import com.farmchainx.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductDto create(ProductDto dto) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setCategory(dto.getCategory());
        product.setDescription(dto.getDescription());
        product.setProductImage(dto.getProductImage());
        return ProductDto.from(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return ProductDto.from(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Product getEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> search(String query, Pageable pageable) {
        return productRepository
                .findByProductNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        query, query, pageable)
                .map(ProductDto::from);
    }

    @Override
    public ProductDto update(Long id, ProductDto dto) {
        Product product = getEntityById(id);

        if (dto.getProductName()  != null) product.setProductName(dto.getProductName());
        if (dto.getCategory()     != null) product.setCategory(dto.getCategory());
        if (dto.getDescription()  != null) product.setDescription(dto.getDescription());
        if (dto.getProductImage() != null) product.setProductImage(dto.getProductImage());

        return ProductDto.from(productRepository.save(product));
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }
}
