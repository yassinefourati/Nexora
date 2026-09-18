package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

/**
 * A loan platform customer — the applicant for a loan product. Distinct from
 * {@link User}, which is an internal/admin identity authenticated via
 * Keycloak.
 *
 * Individual customers populate {@code firstName}/{@code lastName}; business
 * customers populate {@code businessName}. Enforced by
 * {@code customers_individual_name_check} / {@code customers_business_name_check}
 * in V15__customer_domain_core.sql.
 */
@Entity
@Table(name = "customers")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends SoftDeletableEntity {

    @Column(name = "customer_type", length = 20, nullable = false)
    private String customerType;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "active";

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "business_name", length = 200)
    private String businessName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "national_id", length = 50)
    private String nationalId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;
}
