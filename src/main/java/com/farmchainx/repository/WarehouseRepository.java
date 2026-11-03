package com.farmchainx.repository;

import com.farmchainx.entity.User;
import com.farmchainx.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findByManager(User manager);

    Page<Warehouse> findByManager(User manager, Pageable pageable);

    Page<Warehouse> findByWarehouseNameContainingIgnoreCase(String name, Pageable pageable);
}
