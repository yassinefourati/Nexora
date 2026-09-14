package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only audit trail of {@link LoanDisbursement} status transitions.
 * Rows are written by {@code LoanDisbursementService} on every status
 * change — never mutated or deleted. Table has no {@code updated_at}
 * column, so this does not extend {@link BaseEntity}.
 */
@Entity
@Table(name = "loan_disbursement_status_history")
@Getter
@Setter
@NoArgsConstructor
public class LoanDisbursementStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_disbursement_id", nullable = false)
    private LoanDisbursement loanDisbursement;

    @Column(name = "from_status", length = 20)
    private String fromStatus;

    @Column(name = "to_status", length = 20, nullable = false)
    private String toStatus;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt = Instant.now();
}
