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
 * Fraud screening for a {@link LoanApplication} — one per application.
 * Never exposes the underlying detection rules to unauthorized users; only
 * outcomes (score, alerts) are readable through the API. Table has no
 * {@code deleted_at} column, so this extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "fraud_checks")
@Getter
@Setter
@NoArgsConstructor
public class FraudCheck extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "fraud_score")
    private Integer fraudScore;

    @Column(name = "checked_at")
    private Instant checkedAt;
}
