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
 * A consent record on file for a {@link Customer} (data processing, credit
 * check, marketing). Table has no {@code deleted_at} column, so this extends
 * {@link BaseEntity} directly — consent decisions are tracked via
 * {@code grantedAt}/{@code revokedAt}, not row deletion.
 */
@Entity
@Table(name = "customer_consents")
@Getter
@Setter
@NoArgsConstructor
public class CustomerConsent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "consent_type", length = 30, nullable = false)
    private String consentType;

    @Column(name = "granted", nullable = false)
    private boolean granted = false;

    @Column(name = "granted_at")
    private Instant grantedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
