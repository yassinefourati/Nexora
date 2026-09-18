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

import java.time.LocalDate;

/**
 * An identity document on file for a {@link Customer} (passport, national
 * ID, driver's license). Table has no {@code deleted_at} column, so this
 * extends {@link BaseEntity} directly.
 */
@Entity
@Table(name = "customer_identifications")
@Getter
@Setter
@NoArgsConstructor
public class CustomerIdentification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "id_type", length = 20, nullable = false)
    private String idType;

    @Column(name = "id_number", length = 100, nullable = false)
    private String idNumber;

    @Column(name = "issuing_country", length = 2, nullable = false)
    private String issuingCountry;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;
}
