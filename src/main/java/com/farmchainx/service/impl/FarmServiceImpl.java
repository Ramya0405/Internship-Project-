package com.farmchainx.service.impl;

import com.farmchainx.dto.FarmDto;
import com.farmchainx.entity.Farm;
import com.farmchainx.entity.User;
import com.farmchainx.enums.UserRole;
import com.farmchainx.exception.ResourceNotFoundException;
import com.farmchainx.exception.UnauthorizedAccessException;
import com.farmchainx.repository.FarmRepository;
import com.farmchainx.service.FarmService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FarmServiceImpl implements FarmService {

    private final FarmRepository farmRepository;

    public FarmServiceImpl(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }

    @Override
    public FarmDto create(FarmDto dto, User owner) {
        if (owner.getRole() != UserRole.ADMIN && owner.getRole() != UserRole.FARMER) {
            throw new UnauthorizedAccessException("Only farmers or admins can register farms.");
        }
        Farm farm = new Farm();
        farm.setFarmName(dto.getFarmName());
        farm.setLocation(dto.getLocation());
        farm.setFarmSize(dto.getFarmSize());
        farm.setCertification(dto.getCertification());
        farm.setOwner(owner);
        return FarmDto.from(farmRepository.save(farm));
    }

    @Override
    @Transactional(readOnly = true)
    public FarmDto findById(Long id) {
        return FarmDto.from(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public FarmDto findById(Long id, User requestingUser) {
        Farm farm = getEntityById(id);
        assertOwnerOrAdmin(farm, requestingUser);
        return FarmDto.from(farm);
    }

    @Override
    @Transactional(readOnly = true)
    public Farm getEntityById(Long id) {
        return farmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FarmDto> findAll(Pageable pageable) {
        return farmRepository.findAll(pageable).map(FarmDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmDto> findByOwner(User owner) {
        return farmRepository.findByOwner(owner)
                .stream().map(FarmDto::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FarmDto> findByOwner(User owner, Pageable pageable) {
        return farmRepository.findByOwner(owner, pageable).map(FarmDto::from);
    }

    @Override
    public FarmDto update(Long id, FarmDto dto, User requestingUser) {
        Farm farm = getEntityById(id);
        assertOwnerOrAdmin(farm, requestingUser);

        if (dto.getFarmName()      != null) farm.setFarmName(dto.getFarmName());
        if (dto.getLocation()      != null) farm.setLocation(dto.getLocation());
        if (dto.getFarmSize()      != null) farm.setFarmSize(dto.getFarmSize());
        if (dto.getCertification() != null) farm.setCertification(dto.getCertification());

        return FarmDto.from(farmRepository.save(farm));
    }

    @Override
    public void delete(Long id, User requestingUser) {
        Farm farm = getEntityById(id);
        assertOwnerOrAdmin(farm, requestingUser);
        farmRepository.delete(farm);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByOwner(User owner) {
        return farmRepository.countByOwner(owner);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void assertOwnerOrAdmin(Farm farm, User user) {
        boolean isOwner = farm.getOwner().getId().equals(user.getId());
        boolean isAdmin = UserRole.ADMIN.equals(user.getRole());
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedAccessException(
                    "You do not have permission to modify farm: " + farm.getFarmName());
        }
    }
}
