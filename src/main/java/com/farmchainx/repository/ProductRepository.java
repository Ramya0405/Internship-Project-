package com.farmchainx.repository;

import com.farmchainx.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByProductNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name, String category, Pageable pageable);

    boolean existsByProductNameIgnoreCase(String productName);
}
