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
 * A credit bureau report retrieved for a {@link CreditCheck}. Table has no
 * {@code deleted_at} column, so this extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "credit_reports")
@Getter
@Setter
@NoArgsConstructor
public class CreditReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_check_id", nullable = false)
    private CreditCheck creditCheck;

    @Column(name = "bureau_name", length = 50, nullable = false)
    private String bureauName;

    @Column(name = "report_reference", length = 100, nullable = false)
    private String reportReference;

    @Column(name = "raw_score")
    private Integer rawScore;

    @Column(name = "retrieved_at", nullable = false)
    private Instant retrievedAt = Instant.now();
}
