package com.fourati.platform.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method for automatic audit logging.
 * AuditAspect intercepts the call and logs: who, what, how long, success/failure.
 *
 * Usage:
 *   @Audited
 *   public ItemResponse create(CreateItemRequest request) { ... }
 *
 *   @Audited(action = "EXPORT", description = "Export user data as CSV")
 *   public byte[] exportUsers() { ... }
 *
 * Output (INFO level):
 *   [AUDIT] actor=jane@example.com action=CREATE method=ItemService.create duration=12ms status=OK
 *   [AUDIT] actor=jane@example.com action=DELETE method=ItemService.delete duration=5ms status=ERROR: Item not found
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /** Override the action label. Defaults to the method name in UPPER_SNAKE_CASE. */
    String action() default "";

    /** Optional free-text description logged alongside the audit entry. */
    String description() default "";

    /**
     * Logical entity type persisted to the audit_logs.entity_type column
     * (e.g. "user", "role"). Defaults to the declaring class's simple name
     * with "Service" stripped, lower-cased (e.g. UserService -> "user").
     */
    String entityType() default "";
}
