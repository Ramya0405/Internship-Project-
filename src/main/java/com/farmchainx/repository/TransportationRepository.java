package com.farmchainx.repository;

import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.Transportation;
import com.farmchainx.entity.User;
import com.farmchainx.enums.TransportationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportationRepository extends JpaRepository<Transportation, Long> {

    List<Transportation> findByBatchOrderByDepartureDateDesc(ProductBatch batch);

    List<Transportation> findByBatchAndDistributorOrderByDepartureDateDesc(
            ProductBatch batch, User distributor);

    Page<Transportation> findByDistributor(User distributor, Pageable pageable);

    Page<Transportation> findByTransportationStatus(TransportationStatus status, Pageable pageable);

    List<Transportation> findByBatchAndTransportationStatus(ProductBatch batch, TransportationStatus status);
}
