package com.farmchainx.ai;

import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.QualityInspection;
import com.farmchainx.entity.SupplyChainEvent;
import com.farmchainx.entity.Transportation;
import com.farmchainx.enums.EventType;
import com.farmchainx.enums.QualityGrade;
import com.farmchainx.enums.TransportationStatus;
import com.farmchainx.repository.QualityInspectionRepository;
import com.farmchainx.repository.SupplyChainEventRepository;
import com.farmchainx.repository.TransportationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic, rule-based implementation of {@link AgriculturalRiskAnalyzer}.
 *
 * NOTE: This is NOT machine-learning. It applies a transparent set of rules
 * against measurable supply-chain attributes to produce a risk score.
 * Each rule contributes a fixed number of points; scores are additive.
 *
 * Rules and their weights:
 *   1. Product expired                             → +40 pts
 *   2. Quality inspection REJECTED                 → +30 pts
 *   3. Quality grade C (below standard)            → +15 pts
 *   4. Transportation DELAYED                      → +20 pts
 *   5. Temperature > 35°C in any event             → +15 pts
 *   6. Humidity    > 85% in any event              → +10 pts
 *   7. No supply-chain events recorded             → +20 pts
 *   8. Missing harvest event                       → +10 pts
 *   9. Transit time > 72 hours with no warehouse   → +15 pts
 *  10. Quantity discrepancy flag (DAMAGED event)   → +20 pts
 */
@Component
public class RuleBasedRiskAnalyzer implements AgriculturalRiskAnalyzer {

    // Thresholds
    private static final double TEMP_THRESHOLD_CELSIUS = 35.0;
    private static final double HUMIDITY_THRESHOLD_PCT = 85.0;
    private static final long   MAX_TRANSIT_HOURS      = 72;

    private final SupplyChainEventRepository  eventRepository;
    private final QualityInspectionRepository inspectionRepository;
    private final TransportationRepository    transportationRepository;

    public RuleBasedRiskAnalyzer(SupplyChainEventRepository eventRepository,
                                 QualityInspectionRepository inspectionRepository,
                                 TransportationRepository transportationRepository) {
        this.eventRepository          = eventRepository;
        this.inspectionRepository     = inspectionRepository;
        this.transportationRepository = transportationRepository;
    }

    @Override
    public RiskAnalysisResult analyze(ProductBatch batch) {
        List<String> anomalies    = new ArrayList<>();
        List<String> remedies     = new ArrayList<>();
        int          score        = 0;

        // ── Load data ────────────────────────────────────────────────────────
        List<SupplyChainEvent> events       = eventRepository.findByBatchOrderByEventTimestampAsc(batch);
        List<QualityInspection> inspections = inspectionRepository.findByBatchOrderByInspectionDateDesc(batch);
        List<Transportation> transports     = transportationRepository.findByBatchOrderByDepartureDateDesc(batch);

        // ── Rule 1: Expired product ──────────────────────────────────────────
        if (batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(LocalDate.now())) {
            score += 40;
            anomalies.add("Product expired on " + batch.getExpiryDate());
            remedies.add("Immediately quarantine the batch and initiate a recall procedure.");
        }

        // ── Rule 2 & 3: Quality inspection grade ────────────────────────────
        if (!inspections.isEmpty()) {
            QualityInspection latest = inspections.get(0);
            if (QualityGrade.REJECTED.equals(latest.getQualityGrade())) {
                score += 30;
                anomalies.add("Quality inspection FAILED – batch rejected");
                remedies.add("Halt distribution. Notify regulatory authority and initiate disposal process.");
            } else if (QualityGrade.C.equals(latest.getQualityGrade())) {
                score += 15;
                anomalies.add("Quality grade C – below recommended standard");
                remedies.add("Re-inspect batch before further distribution. Consider downgrading or recall.");
            }

            // Temperature at inspection
            if (latest.getTemperature() != null && latest.getTemperature() > TEMP_THRESHOLD_CELSIUS) {
                score += 5;
                anomalies.add("Elevated temperature at inspection: " + latest.getTemperature() + "°C");
                remedies.add("Verify cold-chain integrity. Inspect storage facility refrigeration.");
            }
        }

        // ── Rule 4: Transportation delays ────────────────────────────────────
        long delayedCount = transports.stream()
                .filter(t -> TransportationStatus.DELAYED.equals(t.getTransportationStatus()))
                .count();
        if (delayedCount > 0) {
            int transportScore = (int) Math.min(30, delayedCount * 20);
            score += transportScore;
            anomalies.add(delayedCount + " transportation delay(s) reported");
            remedies.add("Contact carrier and investigate root cause of delay(s).");
        }

        // ── Rules 5 & 6: Abnormal temperature / humidity in events ───────────
        long highTempEvents = events.stream()
                .filter(e -> e.getTemperature() != null && e.getTemperature() > TEMP_THRESHOLD_CELSIUS)
                .count();
        if (highTempEvents > 0) {
            score += 15;
            anomalies.add(highTempEvents + " event(s) recorded temperature above " + TEMP_THRESHOLD_CELSIUS + "°C");
            remedies.add("Audit cold-chain handling. Check vehicle refrigeration and storage conditions.");
        }

        long highHumidityEvents = events.stream()
                .filter(e -> e.getHumidity() != null && e.getHumidity() > HUMIDITY_THRESHOLD_PCT)
                .count();
        if (highHumidityEvents > 0) {
            score += 10;
            anomalies.add(highHumidityEvents + " event(s) recorded humidity above " + HUMIDITY_THRESHOLD_PCT + "%");
            remedies.add("Review storage conditions. High humidity promotes mould and spoilage.");
        }

        // ── Rule 7: No supply-chain events ──────────────────────────────────
        if (events.isEmpty()) {
            score += 20;
            anomalies.add("No supply-chain events recorded for this batch");
            remedies.add("Ensure all supply-chain participants are recording events in the system.");
        } else {
            // ── Rule 8: Missing HARVESTED event ─────────────────────────────
            boolean hasHarvestEvent = events.stream()
                    .anyMatch(e -> EventType.HARVESTED.equals(e.getEventType()));
            if (!hasHarvestEvent) {
                score += 10;
                anomalies.add("No HARVESTED event recorded – origin traceability gap");
                remedies.add("Request farmer to record the initial harvest event.");
            }
        }

        // ── Rule 9: Long transit without warehouse arrival ───────────────────
        if (!transports.isEmpty()) {
            Transportation first = transports.get(transports.size() - 1); // oldest
            if (first.getDepartureDate() != null) {
                long hoursInTransit = ChronoUnit.HOURS.between(
                        first.getDepartureDate(), LocalDateTime.now());
                boolean hasWarehouseEvent = events.stream()
                        .anyMatch(e -> EventType.ARRIVED_AT_WAREHOUSE.equals(e.getEventType()));
                if (hoursInTransit > MAX_TRANSIT_HOURS && !hasWarehouseEvent) {
                    score += 15;
                    anomalies.add("Batch in transit for " + hoursInTransit + "h without warehouse arrival recorded");
                    remedies.add("Investigate shipment status. Confirm delivery with warehouse team.");
                }
            }
        }

        // ── Rule 10: Damaged event ───────────────────────────────────────────
        boolean hasDamageEvent = events.stream()
                .anyMatch(e -> EventType.DAMAGED.equals(e.getEventType()));
        if (hasDamageEvent) {
            score += 20;
            anomalies.add("DAMAGED event recorded for this batch");
            remedies.add("Inspect batch for extent of damage. File insurance claim. Consider disposal.");
        }

        // ── Build recommendations string ─────────────────────────────────────
        String recommendation = anomalies.isEmpty()
                ? "No anomalies detected. Continue normal supply-chain operations."
                : String.join(" | ", remedies);

        return new RiskAnalysisResult(score, anomalies, recommendation);
    }
}
