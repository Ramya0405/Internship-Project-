package com.farmchainx.service;

import com.farmchainx.dto.WarehouseDto;
import com.farmchainx.entity.User;
import com.farmchainx.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WarehouseService {

    WarehouseDto create(WarehouseDto dto, User manager);

    WarehouseDto findById(Long id);

    WarehouseDto findById(Long id, User requestingUser);

    Warehouse getEntityById(Long id);

    Page<WarehouseDto> findAll(Pageable pageable);

    Page<WarehouseDto> findAll(Pageable pageable, User requestingUser);

    List<WarehouseDto> findByManager(User manager);

    WarehouseDto update(Long id, WarehouseDto dto, User actor);
}
