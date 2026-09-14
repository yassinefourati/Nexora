package com.fourati.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Auditing and transaction wiring.
 *
 * <p>{@code @EnableJpaAuditing} populates {@code @CreatedDate}/{@code @LastModifiedDate}
 * on {@code BaseEntity}. No {@code AuditorAware} is registered: the schema has no
 * created_by/updated_by columns; actor tracking goes through {@code audit_logs}.
 *
 * <p>{@code @EnableTransactionManagement(order = ...)} pins the transaction advisor
 * <em>outside</em> the {@code AuditAspect} advisor (ADR 0007). Both default to
 * {@code LOWEST_PRECEDENCE}, which leaves their nesting undefined; giving the
 * transaction interceptor a lower order value makes it the outermost proxy, so an
 * {@code @Audited @Transactional} method publishes its {@code AuditEvent} while the
 * transaction is still open and the BEFORE_COMMIT listener writes the audit row in
 * the same transaction. {@code AuditTransactionIntegrationTest} proves both the
 * atomic write and the rollback.
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement(order = AuditConfig.TRANSACTION_ADVISOR_ORDER, proxyTargetClass = true)
public class AuditConfig {

    /** Outside every default-ordered aspect (audit, caching), inside nothing we own. */
    public static final int TRANSACTION_ADVISOR_ORDER = Ordered.LOWEST_PRECEDENCE - 100;
}
