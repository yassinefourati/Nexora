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
 * The overall risk assessment for a {@link LoanApplication} — one per
 * application. Status transitions are recorded in
 * {@link RiskAssessmentStatusHistory} rather than only overwritten in
 * place. Table has no {@code deleted_at} column, so this extends
 * {@link BaseEntity} directly.
 */
@Entity
@Table(name = "risk_assessments")
@Getter
@Setter
@NoArgsConstructor
public class RiskAssessment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "risk_class", length = 20)
    private String riskClass;

    @Column(name = "assessed_at")
    private Instant assessedAt;
}
