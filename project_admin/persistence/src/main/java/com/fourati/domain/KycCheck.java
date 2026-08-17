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
 * An individual verification check performed within a {@link KycCase}
 * (identity document, liveness, address, sanctions screening). Table has
 * no {@code deleted_at} column, so this extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "kyc_checks")
@Getter
@Setter
@NoArgsConstructor
public class KycCheck extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kyc_case_id", nullable = false)
    private KycCase kycCase;

    @Column(name = "check_type", length = 30, nullable = false)
    private String checkType;

    @Column(name = "result", length = 20, nullable = false)
    private String result;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt = Instant.now();
}
