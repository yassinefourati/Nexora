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
 * A configurable per-{@link LoanProduct} checklist entry: which document
 * type is required (and whether it's mandatory) to complete an application
 * for that product. Table has no {@code deleted_at} column, so this
 * extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "document_requirements")
@Getter
@Setter
@NoArgsConstructor
public class DocumentRequirement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "document_type", length = 30, nullable = false)
    private String documentType;

    @Column(name = "is_mandatory", nullable = false)
    private boolean mandatory = true;
}
