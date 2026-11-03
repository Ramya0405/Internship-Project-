package com.farmchainx.repository;

import com.farmchainx.entity.Farm;
import com.farmchainx.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {

    List<Farm> findByOwner(User owner);

    Page<Farm> findByOwner(User owner, Pageable pageable);

    Page<Farm> findByFarmNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByFarmNameAndOwner(String farmName, User owner);

    long countByOwner(User owner);
}
