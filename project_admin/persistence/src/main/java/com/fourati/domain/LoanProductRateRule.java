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
 * An interest rate rule for a {@link LoanProduct} (fixed or variable, with
 * an optional margin over an index for variable rates). Table has no
 * {@code deleted_at} column, so this extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "loan_product_rate_rules")
@Getter
@Setter
@NoArgsConstructor
public class LoanProductRateRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "rate_type", length = 20, nullable = false)
    private String rateType;

    @Column(name = "base_rate", precision = 6, scale = 4, nullable = false)
    private BigDecimal baseRate;

    @Column(name = "margin", precision = 6, scale = 4)
    private BigDecimal margin;
}
