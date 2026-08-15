package com.fourati.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so {@code @CreatedDate}/{@code @LastModifiedDate}
 * (used by BaseEntity/AuditableEntity's createdAt/updatedAt) are populated
 * automatically on insert/update. No AuditorAware bean is registered — this
 * schema has no created_by/updated_by column on any table (see BaseEntity's
 * javadoc), so there is no actor to resolve here. Actor tracking for writes
 * goes entirely through audit_logs (see AuditAspect/AuditLogEventListener),
 * which correctly resolves identity via the JWT's preferred_username claim.
 */
@Configuration
@EnableJpaAuditing
public class AuditConfig {
}
