package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * An alert raised from an {@link AmlScreening} match, requiring manual
 * investigation. Table has no {@code deleted_at} column, so this extends
 * {@link BaseEntity} directly.
 */
@Entity
@Table(name = "aml_alerts")
@Getter
@Setter
@NoArgsConstructor
public class AmlAlert extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aml_screening_id", nullable = false)
    private AmlScreening amlScreening;

    @Column(name = "alert_type", length = 30, nullable = false)
    private String alertType;

    @Column(name = "severity", length = 20, nullable = false)
    private String severity = "medium";

    @Column(name = "status", length = 20, nullable = false)
    private String status = "open";

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
