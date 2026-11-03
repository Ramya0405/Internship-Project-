package com.farmchainx.repository;

import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.SupplyChainEvent;
import com.farmchainx.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SupplyChainEventRepository extends JpaRepository<SupplyChainEvent, Long> {

    List<SupplyChainEvent> findByBatchOrderByEventTimestampAsc(ProductBatch batch);

    List<SupplyChainEvent> findByBatchOrderByEventTimestampDesc(ProductBatch batch);

    /** Count events of a specific type for a batch (used by AI analyzer). */
    long countByBatchAndEventType(ProductBatch batch, EventType eventType);

    /** Latest event for a batch regardless of type. */
    @Query("SELECT e FROM SupplyChainEvent e WHERE e.batch = :batch ORDER BY e.eventTimestamp DESC LIMIT 1")
    java.util.Optional<SupplyChainEvent> findLatestEventForBatch(@Param("batch") ProductBatch batch);

    /** Events with temperature readings above threshold (used by AI analyzer). */
    @Query("SELECT e FROM SupplyChainEvent e WHERE e.batch = :batch AND e.temperature IS NOT NULL AND e.temperature > :threshold")
    List<SupplyChainEvent> findHighTemperatureEvents(@Param("batch") ProductBatch batch,
                                                      @Param("threshold") double threshold);

    /** Events with humidity readings above threshold (used by AI analyzer). */
    @Query("SELECT e FROM SupplyChainEvent e WHERE e.batch = :batch AND e.humidity IS NOT NULL AND e.humidity > :threshold")
    List<SupplyChainEvent> findHighHumidityEvents(@Param("batch") ProductBatch batch,
                                                   @Param("threshold") double threshold);

    /** How many hours elapsed between first and last event for a batch. */
    @Query("SELECT TIMESTAMPDIFF(HOUR, MIN(e.eventTimestamp), MAX(e.eventTimestamp)) " +
           "FROM SupplyChainEvent e WHERE e.batch = :batch")
    Long calculateTotalTransitHours(@Param("batch") ProductBatch batch);

    List<SupplyChainEvent> findByEventTimestampBetween(LocalDateTime start, LocalDateTime end);
}
