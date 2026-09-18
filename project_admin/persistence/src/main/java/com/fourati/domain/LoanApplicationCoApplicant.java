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

/**
 * A co-applicant (spouse, family member, business partner, guarantor, ...)
 * on a {@link LoanApplication}. Table has no {@code deleted_at} column, so
 * this extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "loan_application_co_applicants")
@Getter
@Setter
@NoArgsConstructor
public class LoanApplicationCoApplicant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "relationship_type", length = 30, nullable = false)
    private String relationshipType;
}
