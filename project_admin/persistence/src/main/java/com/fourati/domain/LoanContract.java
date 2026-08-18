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
 * The contract document generated from an accepted {@link LoanOffer},
 * carrying the final terms into a document the customer will sign.
 * Signature capture itself belongs to a later Signature module — this
 * entity only tracks the contract document's own lifecycle. References
 * both the application and the offer it was generated from — the only
 * other module this one is coupled to. Status transitions are recorded in
 * {@link LoanContractStatusHistory} rather than only overwriting
 * {@code status} in place.
 */
@Entity
@Table(name = "loan_contracts")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class LoanContract extends SoftDeletableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_offer_id", nullable = false)
    private LoanOffer loanOffer;

    @Column(name = "contract_number", length = 50, nullable = false)
    private String contractNumber;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "draft";

    @Column(name = "principal_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Column(name = "interest_rate", precision = 6, scale = 3, nullable = false)
    private BigDecimal interestRate;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "finalized_at")
    private Instant finalizedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", length = 1000)
    private String cancellationReason;
}
