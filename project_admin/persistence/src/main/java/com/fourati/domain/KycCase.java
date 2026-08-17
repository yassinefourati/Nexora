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
 * A customer identity-verification case (Know Your Customer). Status
 * transitions are recorded in {@link KycStatusHistory} rather than only
 * overwritten in place. Table has no {@code deleted_at} column, so this
 * extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "kyc_cases")
@Getter
@Setter
@NoArgsConstructor
public class KycCase extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "initiated_at", nullable = false)
    private Instant initiatedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;
}
