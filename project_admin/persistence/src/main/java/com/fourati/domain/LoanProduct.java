package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * A configurable loan product offered by the platform (personal, auto,
 * mortgage, etc.). New products are introduced through configuration here
 * rather than code changes to origination/underwriting logic.
 */
@Entity
@Table(name = "loan_products")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class LoanProduct extends SoftDeletableEntity {

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "product_type", length = 30, nullable = false)
    private String productType;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "active";

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "USD";

    @Column(name = "min_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal maxAmount;

    @Column(name = "min_term_months", nullable = false)
    private int minTermMonths;

    @Column(name = "max_term_months", nullable = false)
    private int maxTermMonths;

    @Column(name = "description")
    private String description;
}
