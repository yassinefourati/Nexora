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
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A customer's application for a specific loan product. Status transitions
 * are recorded in {@link LoanApplicationStatusHistory} rather than only
 * overwriting {@code status} in place.
 */
@Entity
@Table(name = "loan_applications")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class LoanApplication extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "draft";

    @Column(name = "requested_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "requested_term_months", nullable = false)
    private int requestedTermMonths;

    @Column(name = "purpose", length = 200)
    private String purpose;

    @Column(name = "submitted_at")
    private Instant submittedAt;
}
