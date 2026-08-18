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
 * A condition attached to a loan approval (e.g. proof of insurance, down
 * payment confirmation). Table has no {@code deleted_at} column, so this
 * extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "loan_approval_conditions")
@Getter
@Setter
@NoArgsConstructor
public class LoanApprovalCondition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_approval_id", nullable = false)
    private LoanApproval loanApproval;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "satisfied_at")
    private Instant satisfiedAt;
}
