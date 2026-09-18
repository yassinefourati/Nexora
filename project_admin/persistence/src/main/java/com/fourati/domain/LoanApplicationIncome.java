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
 * A declared income source on a {@link LoanApplication}. Table has no
 * {@code deleted_at} column, so this extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "loan_application_incomes")
@Getter
@Setter
@NoArgsConstructor
public class LoanApplicationIncome extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(name = "income_type", length = 30, nullable = false)
    private String incomeType;

    @Column(name = "monthly_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal monthlyAmount;

    @Column(name = "source", length = 200)
    private String source;
}
