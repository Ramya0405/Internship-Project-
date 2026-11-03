package com.farmchainx.dto;

import com.farmchainx.entity.QualityInspection;
import com.farmchainx.enums.QualityGrade;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO for quality-inspection records.
 */
public class QualityInspectionDto {

    private Long id;

    @NotNull(message = "Batch ID is required")
    private Long batchId;
    private String trackingId;

    private Long inspectedById;
    private String inspectedByName;

    @NotNull(message = "Quality grade is required")
    private QualityGrade qualityGrade;

    private Double temperature;
    private Double humidity;
    private String inspectionNotes;

    @NotNull(message = "Inspection date is required")
    private LocalDate inspectionDate;

    public QualityInspectionDto() {}

    public static QualityInspectionDto from(QualityInspection qi) {
        QualityInspectionDto dto = new QualityInspectionDto();
        dto.id               = qi.getId();
        dto.batchId          = qi.getBatch()       != null ? qi.getBatch().getId()              : null;
        dto.trackingId       = qi.getBatch()       != null ? qi.getBatch().getTrackingId()       : null;
        dto.inspectedById    = qi.getInspectedBy() != null ? qi.getInspectedBy().getId()         : null;
        dto.inspectedByName  = qi.getInspectedBy() != null ? qi.getInspectedBy().getFullName()   : null;
        dto.qualityGrade     = qi.getQualityGrade();
        dto.temperature      = qi.getTemperature();
        dto.humidity         = qi.getHumidity();
        dto.inspectionNotes  = qi.getInspectionNotes();
        dto.inspectionDate   = qi.getInspectionDate();
        return dto;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }

    public Long getInspectedById() { return inspectedById; }
    public void setInspectedById(Long inspectedById) { this.inspectedById = inspectedById; }

    public String getInspectedByName() { return inspectedByName; }
    public void setInspectedByName(String inspectedByName) { this.inspectedByName = inspectedByName; }

    public QualityGrade getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(QualityGrade qualityGrade) { this.qualityGrade = qualityGrade; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }

    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }
}
