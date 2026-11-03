package com.farmchainx.entity;

import com.farmchainx.enums.QualityGrade;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Quality inspection record for a product batch.
 */
@Entity
@Table(
    name = "quality_inspections",
    indexes = {
        @Index(name = "idx_qi_batch",       columnList = "batch_id"),
        @Index(name = "idx_qi_inspected_by",columnList = "inspected_by_id"),
        @Index(name = "idx_qi_grade",       columnList = "quality_grade")
    }
)
public class QualityInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Batch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private ProductBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspected_by_id")
    private User inspectedBy;

    @NotNull(message = "Quality grade is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "quality_grade", nullable = false, length = 20)
    private QualityGrade qualityGrade;

    @Column
    private Double temperature; // Celsius at time of inspection

    @Column
    private Double humidity;    // % at time of inspection

    @Column(name = "inspection_notes", length = 1000)
    private String inspectionNotes;

    @NotNull(message = "Inspection date is required")
    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    // ── Constructors ─────────────────────────────────────────────────────────

    public QualityInspection() {}

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public ProductBatch getBatch() { return batch; }
    public void setBatch(ProductBatch batch) { this.batch = batch; }

    public User getInspectedBy() { return inspectedBy; }
    public void setInspectedBy(User inspectedBy) { this.inspectedBy = inspectedBy; }

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
