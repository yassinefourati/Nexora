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
 * A customer payment captured against one specific {@link LoanInstallment}.
 * References the account and the installment it pays off — the only other
 * module this one is coupled to; on completion the service layer marks the
 * installment paid and reduces the account's outstanding principal rather
 * than this table holding that state itself. Table has no {@code
 * deleted_at} column, so this extends {@link BaseEntity} directly. Status
 * transitions are recorded in {@link LoanRepaymentStatusHistory} rather
 * than only overwriting {@code status} in place.
 */
@Entity
@Table(name = "loan_repayments")
@Getter
@Setter
@NoArgsConstructor
public class LoanRepayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id", nullable = false)
    private LoanAccount loanAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_installment_id", nullable = false)
    private LoanInstallment loanInstallment;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 20, nullable = false)
    private String paymentMethod;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "failed_at")
    private Instant failedAt;
}
