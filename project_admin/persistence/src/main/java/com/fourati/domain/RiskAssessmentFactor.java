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

/**
 * One weighted input factor (credit score, DTI ratio, etc.) that fed into
 * a {@link RiskAssessment}. Table has no {@code deleted_at} column, so this
 * extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "risk_assessment_factors")
@Getter
@Setter
@NoArgsConstructor
public class RiskAssessmentFactor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_assessment_id", nullable = false)
    private RiskAssessment riskAssessment;

    @Column(name = "factor_type", length = 30, nullable = false)
    private String factorType;

    @Column(name = "factor_value", precision = 10, scale = 4, nullable = false)
    private BigDecimal factorValue;

    @Column(name = "weight", precision = 5, scale = 4, nullable = false)
    private BigDecimal weight;
}
