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
 * A version snapshot of a {@link LoanProduct}. Running loan applications
 * reference the version that was active when they were created, so a
 * product's terms can change going forward without altering applications
 * already in flight. Table has no {@code deleted_at} column, so this
 * extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "loan_product_versions")
@Getter
@Setter
@NoArgsConstructor
public class LoanProductVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "draft";

    @Column(name = "effective_from")
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;
}
