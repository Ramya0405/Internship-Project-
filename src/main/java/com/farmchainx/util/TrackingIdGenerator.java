package com.farmchainx.util;

import com.farmchainx.config.AppProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Generates unique tracking IDs for agricultural product batches.
 *
 * Format: {PREFIX}-YYYYMMDD-XXXXXXXX
 * Example: FCX-20240515-A1B2C3D4
 */
@Component
public class TrackingIdGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AppProperties appProperties;

    public TrackingIdGenerator(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public TrackingIdGenerator() {
        this.appProperties = null;
    }

    /**
     * Generates a new unique tracking ID.
     *
     * @return a unique tracking ID string in the format {PREFIX}-YYYYMMDD-XXXXXXXX
     */
    public String generate() {
        String prefix = (appProperties != null && appProperties.getTracking() != null && appProperties.getTracking().getPrefix() != null)
                ? appProperties.getTracking().getPrefix()
                : "FCX";
        String datePart = LocalDate.now().format(DATE_FORMAT);
        String uniquePart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
        return prefix + "-" + datePart + "-" + uniquePart;
    }
}
