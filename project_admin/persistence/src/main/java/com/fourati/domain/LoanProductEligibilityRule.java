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
 * The minimum eligibility thresholds for a {@link LoanProduct} — one row
 * per product (enforced by {@code loan_product_eligibility_rules_product_key}
 * in V16__loan_product_core.sql). Table has no {@code deleted_at} column, so
 * this extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "loan_product_eligibility_rules")
@Getter
@Setter
@NoArgsConstructor
public class LoanProductEligibilityRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "min_credit_score")
    private Integer minCreditScore;

    @Column(name = "min_monthly_income", precision = 15, scale = 2)
    private BigDecimal minMonthlyIncome;

    @Column(name = "max_debt_to_income_ratio", precision = 5, scale = 4)
    private BigDecimal maxDebtToIncomeRatio;

    @Column(name = "min_age")
    private Integer minAge;
}
