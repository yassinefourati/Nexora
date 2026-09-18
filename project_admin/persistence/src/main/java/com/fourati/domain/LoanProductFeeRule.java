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
 * A fee rule for a {@link LoanProduct} (origination, late payment, early
 * repayment, processing), expressed as a flat amount and/or a percentage.
 * Table has no {@code deleted_at} column, so this extends {@link BaseEntity}
 * directly.
 */
@Entity
@Table(name = "loan_product_fee_rules")
@Getter
@Setter
@NoArgsConstructor
public class LoanProductFeeRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "fee_type", length = 30, nullable = false)
    private String feeType;

    @Column(name = "fee_amount", precision = 15, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "fee_percentage", precision = 6, scale = 4)
    private BigDecimal feePercentage;

    @Column(name = "is_mandatory", nullable = false)
    private boolean mandatory = true;
}
