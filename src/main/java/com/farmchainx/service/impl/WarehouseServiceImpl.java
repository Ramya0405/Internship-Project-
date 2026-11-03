package com.farmchainx.service.impl;

import com.farmchainx.dto.WarehouseDto;
import com.farmchainx.entity.User;
import com.farmchainx.entity.Warehouse;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.repository.WarehouseRepository;
import com.farmchainx.service.WarehouseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public WarehouseDto create(WarehouseDto dto, User manager) {
        if (manager.getRole() != UserRole.ADMIN && manager.getRole() != UserRole.WAREHOUSE_MANAGER) {
            throw new UnauthorizedAccessException("Only warehouse managers or admins can create warehouses.");
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseName(dto.getWarehouseName());
        warehouse.setLocation(dto.getLocation());
        warehouse.setManager(manager);
        warehouse.setCapacityTons(dto.getCapacityTons());
        return WarehouseDto.from(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDto findById(Long id) {
        return WarehouseDto.from(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDto findById(Long id, User requestingUser) {
        Warehouse warehouse = getEntityById(id);
        assertCanManage(warehouse, requestingUser);
        return WarehouseDto.from(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public Warehouse getEntityById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseDto> findAll(Pageable pageable) {
        return warehouseRepository.findAll(pageable).map(WarehouseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseDto> findAll(Pageable pageable, User requestingUser) {
        if (UserRole.ADMIN.equals(requestingUser.getRole())) {
            return findAll(pageable);
        }
        if (requestingUser.getRole() != UserRole.WAREHOUSE_MANAGER) {
            throw new UnauthorizedAccessException("Only warehouse managers or admins can view warehouses.");
        }
        return warehouseRepository.findByManager(requestingUser, pageable).map(WarehouseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDto> findByManager(User manager) {
        return warehouseRepository.findByManager(manager)
                .stream().map(WarehouseDto::from).collect(Collectors.toList());
    }

    @Override
    public WarehouseDto update(Long id, WarehouseDto dto, User actor) {
        Warehouse warehouse = getEntityById(id);
        assertCanManage(warehouse, actor);
        if (dto.getWarehouseName() != null) warehouse.setWarehouseName(dto.getWarehouseName());
        if (dto.getLocation()      != null) warehouse.setLocation(dto.getLocation());
        if (dto.getCapacityTons()  != null) warehouse.setCapacityTons(dto.getCapacityTons());
        return WarehouseDto.from(warehouseRepository.save(warehouse));
    }

    private void assertCanManage(Warehouse warehouse, User actor) {
        if (UserRole.ADMIN.equals(actor.getRole())) {
            return;
        }
        if (actor.getRole() != UserRole.WAREHOUSE_MANAGER
                || warehouse.getManager() == null
                || !warehouse.getManager().getId().equals(actor.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to manage this warehouse.");
        }
    }
}
