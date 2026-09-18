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
 * A normalized credit score computed for a {@link CreditCheck}. Table has
 * no {@code deleted_at} column, so this extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "credit_scores")
@Getter
@Setter
@NoArgsConstructor
public class CreditScore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_check_id", nullable = false)
    private CreditCheck creditCheck;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "score_model", length = 50, nullable = false)
    private String scoreModel;

    @Column(name = "scored_at", nullable = false)
    private Instant scoredAt = Instant.now();
}
