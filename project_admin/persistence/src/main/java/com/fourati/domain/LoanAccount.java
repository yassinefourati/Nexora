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
 * The servicing account opened once a {@link LoanDisbursement} completes,
 * carrying the funded loan into its repayment life. Its reducing-balance
 * {@link LoanInstallment} schedule is generated at opening time. Payment
 * capture against installments belongs to a later Repayment module — this
 * entity only tracks the account and the schedule it owes against.
 * References the application and the disbursement it was opened from — the
 * only other module this one is coupled to. Status transitions are
 * recorded in {@link LoanAccountStatusHistory} rather than only
 * overwriting {@code status} in place.
 */
@Entity
@Table(name = "loan_accounts")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class LoanAccount extends SoftDeletableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_disbursement_id", nullable = false)
    private LoanDisbursement loanDisbursement;

    @Column(name = "account_number", length = 50, nullable = false)
    private String accountNumber;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "active";

    @Column(name = "principal_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", precision = 6, scale = 3, nullable = false)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Column(name = "outstanding_principal", precision = 15, scale = 2, nullable = false)
    private BigDecimal outstandingPrincipal;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;
}
