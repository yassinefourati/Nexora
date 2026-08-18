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

import java.time.Instant;

/**
 * One signer's signing request against a {@link LoanContract}. A contract
 * may require more than one signature (primary applicant plus
 * co-applicants/guarantors), so this is a one-to-many child rather than a
 * 1:1 extension. Table has no {@code deleted_at} column, so this extends
 * {@link BaseEntity} directly. Status transitions are recorded in
 * {@link ContractSignatureStatusHistory} rather than only overwriting
 * {@code status} in place.
 */
@Entity
@Table(name = "contract_signatures")
@Getter
@Setter
@NoArgsConstructor
public class ContractSignature extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_contract_id", nullable = false)
    private LoanContract loanContract;

    @Column(name = "signer_name", length = 200, nullable = false)
    private String signerName;

    @Column(name = "signer_role", length = 20, nullable = false)
    private String signerRole;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "signature_method", length = 20, nullable = false)
    private String signatureMethod = "electronic";

    @Column(name = "decline_reason", length = 1000)
    private String declineReason;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt = Instant.now();

    @Column(name = "signed_at")
    private Instant signedAt;

    @Column(name = "declined_at")
    private Instant declinedAt;
}
