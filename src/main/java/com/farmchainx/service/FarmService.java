package com.farmchainx.service;

import com.farmchainx.dto.FarmDto;
import com.farmchainx.entity.Farm;
import com.farmchainx.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FarmService {

    FarmDto create(FarmDto dto, User owner);

    FarmDto findById(Long id);

    FarmDto findById(Long id, User requestingUser);

    Farm getEntityById(Long id);

    Page<FarmDto> findAll(Pageable pageable);

    List<FarmDto> findByOwner(User owner);

    Page<FarmDto> findByOwner(User owner, Pageable pageable);

    FarmDto update(Long id, FarmDto dto, User requestingUser);

    void delete(Long id, User requestingUser);

    long countByOwner(User owner);
}
