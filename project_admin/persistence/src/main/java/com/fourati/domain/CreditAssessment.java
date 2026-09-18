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

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The credit decision derived from a {@link CreditCheck} — one per check.
 * Table has no {@code deleted_at} column, so this extends
 * {@link BaseEntity} directly.
 */
@Entity
@Table(name = "credit_assessments")
@Getter
@Setter
@NoArgsConstructor
public class CreditAssessment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_check_id", nullable = false)
    private CreditCheck creditCheck;

    @Column(name = "debt_to_income_ratio", precision = 5, scale = 4)
    private BigDecimal debtToIncomeRatio;

    @Column(name = "decision", length = 20, nullable = false)
    private String decision;

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt = Instant.now();
}
