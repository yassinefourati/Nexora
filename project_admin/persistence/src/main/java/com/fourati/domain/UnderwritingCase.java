package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The underwriting decision for a {@link LoanApplication}. Deliberately does
 * not reference credit/risk/fraud entities directly — those are read
 * independently, scoped by loan_application_id, to keep module boundaries
 * intact. Status transitions are recorded in
 * {@link UnderwritingStatusHistory} rather than only overwriting
 * {@code status} in place.
 */
@Entity
@Table(name = "underwriting_cases")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class UnderwritingCase extends SoftDeletableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "decision", length = 30)
    private String decision;

    @Column(name = "decision_reason", length = 1000)
    private String decisionReason;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "approved_term_months")
    private Integer approvedTermMonths;

    @Column(name = "assigned_to", length = 150)
    private String assignedTo;

    @Column(name = "decided_at")
    private Instant decidedAt;
}
