package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Tracks collection efforts on one overdue {@link LoanInstallment} through
 * to resolution or write-off. References the account and the overdue
 * installment that triggered the case — the only other module this one is
 * coupled to. Status transitions are recorded in {@link
 * CollectionCaseStatusHistory} rather than only overwriting {@code status}
 * in place.
 */
@Entity
@Table(name = "collection_cases")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class CollectionCase extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id", nullable = false)
    private LoanAccount loanAccount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_installment_id", nullable = false)
    private LoanInstallment loanInstallment;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "open";

    @Column(name = "stage", length = 20, nullable = false)
    private String stage = "reminder";

    @Column(name = "assigned_to", length = 150)
    private String assignedTo;

    @Column(name = "overdue_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal overdueAmount;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
