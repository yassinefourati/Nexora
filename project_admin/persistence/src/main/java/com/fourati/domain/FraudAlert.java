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
 * An alert raised from a {@link FraudCheck} indicator, requiring manual
 * investigation. Table has no {@code deleted_at} column, so this extends
 * {@link BaseEntity} directly.
 */
@Entity
@Table(name = "fraud_alerts")
@Getter
@Setter
@NoArgsConstructor
public class FraudAlert extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fraud_check_id", nullable = false)
    private FraudCheck fraudCheck;

    @Column(name = "indicator_type", length = 30, nullable = false)
    private String indicatorType;

    @Column(name = "severity", length = 20, nullable = false)
    private String severity = "medium";

    @Column(name = "status", length = 20, nullable = false)
    private String status = "open";

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
