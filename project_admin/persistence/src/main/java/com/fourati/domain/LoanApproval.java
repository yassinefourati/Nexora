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
 * Formalizes a completed {@link UnderwritingCase} decision into approved
 * terms for a {@link LoanApplication}. References both the application and
 * the underwriting case it stems from — the only other module this one is
 * coupled to — but not credit/risk/fraud entities directly, for the same
 * module-boundary reason applied to underwriting. Status transitions are
 * recorded in {@link LoanApprovalStatusHistory} rather than only
 * overwriting {@code status} in place.
 */
@Entity
@Table(name = "loan_approvals")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class LoanApproval extends SoftDeletableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "underwriting_case_id", nullable = false)
    private UnderwritingCase underwritingCase;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "approved_term_months")
    private Integer approvedTermMonths;

    @Column(name = "interest_rate", precision = 6, scale = 3)
    private BigDecimal interestRate;

    @Column(name = "approved_by", length = 150)
    private String approvedBy;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "approved_at")
    private Instant approvedAt;
}
