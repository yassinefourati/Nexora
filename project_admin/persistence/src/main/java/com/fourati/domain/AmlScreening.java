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
 * An Anti-Money-Laundering screening result (sanctions/PEP/watchlist) for
 * a {@link KycCase}. Table has no {@code deleted_at} column, so this
 * extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "aml_screenings")
@Getter
@Setter
@NoArgsConstructor
public class AmlScreening extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kyc_case_id", nullable = false)
    private KycCase kycCase;

    @Column(name = "screening_type", length = 20, nullable = false)
    private String screeningType;

    @Column(name = "result", length = 20, nullable = false)
    private String result;

    @Column(name = "screened_at", nullable = false)
    private Instant screenedAt = Instant.now();
}
