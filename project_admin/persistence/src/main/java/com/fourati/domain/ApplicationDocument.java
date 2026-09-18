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
 * Links a {@link Document} to the {@link LoanApplication} it was uploaded
 * for, and the requirement it satisfies. Table has no {@code deleted_at}
 * column, so this extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "application_documents")
@Getter
@Setter
@NoArgsConstructor
public class ApplicationDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "requirement_type", length = 30, nullable = false)
    private String requirementType;
}
