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
 * A condition attached to an "approve with conditions" underwriting
 * decision (e.g. proof of income, additional collateral). Table has no
 * {@code deleted_at} column, so this extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "underwriting_conditions")
@Getter
@Setter
@NoArgsConstructor
public class UnderwritingCondition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "underwriting_case_id", nullable = false)
    private UnderwritingCase underwritingCase;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "satisfied_at")
    private Instant satisfiedAt;
}
