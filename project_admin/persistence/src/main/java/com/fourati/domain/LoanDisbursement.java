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
 * Releases the principal funds for a {@link LoanApplication} once its
 * {@link LoanContract} is fully signed. References the application and the
 * contract it disburses against — the only other module this one is
 * coupled to; signature completeness is checked by the service layer via
 * {@link ContractSignature} rather than an FK. Status transitions are
 * recorded in {@link LoanDisbursementStatusHistory} rather than only
 * overwriting {@code status} in place.
 */
@Entity
@Table(name = "loan_disbursements")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class LoanDisbursement extends SoftDeletableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_contract_id", nullable = false)
    private LoanContract loanContract;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "disbursement_method", length = 20, nullable = false)
    private String disbursementMethod;

    @Column(name = "destination_account", length = 100, nullable = false)
    private String destinationAccount;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "initiated_at")
    private Instant initiatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failed_at")
    private Instant failedAt;
}
