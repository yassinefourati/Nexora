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
import java.time.LocalDate;

/**
 * One row of a {@link LoanAccount}'s reducing-balance repayment schedule.
 * Table has no {@code deleted_at} column, so this extends {@link
 * BaseEntity} directly. Payment capture against installments belongs to a
 * later Repayment module — this entity only tracks what is owed and its
 * pending/paid/overdue status.
 */
@Entity
@Table(name = "loan_installments")
@Getter
@Setter
@NoArgsConstructor
public class LoanInstallment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "installment_number", nullable = false)
    private int installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "principal_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal interestAmount;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";
}
