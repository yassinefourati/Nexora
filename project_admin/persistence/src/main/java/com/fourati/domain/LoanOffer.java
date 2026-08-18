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
 * Presents an approved {@link LoanApproval}'s terms to the customer for
 * acceptance or decline. References both the application and the approval
 * it presents — the only other module this one is coupled to. Status
 * transitions are recorded in {@link LoanOfferStatusHistory} rather than
 * only overwriting {@code status} in place.
 */
@Entity
@Table(name = "loan_offers")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class LoanOffer extends SoftDeletableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_approval_id", nullable = false)
    private LoanApproval loanApproval;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "issued";

    @Column(name = "offered_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal offeredAmount;

    @Column(name = "offered_term_months", nullable = false)
    private int offeredTermMonths;

    @Column(name = "interest_rate", precision = 6, scale = 3, nullable = false)
    private BigDecimal interestRate;

    @Column(name = "decline_reason", length = 1000)
    private String declineReason;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "declined_at")
    private Instant declinedAt;
}
