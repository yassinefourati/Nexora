package com.fourati.platform.audit;

import com.fourati.platform.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * AOP aspect that intercepts @Audited methods, logs a structured audit entry,
 * and publishes an {@link AuditEvent} for any interested listener to persist.
 *
 * Deliberately domain-agnostic: this class lives under platform.. and must never
 * depend on this app's own domain/repository code, so persisting the event to a
 * database table is the responsibility of a domain-side @EventListener, not this
 * aspect. See com.fourati.audit.internal.application.AuditLogEventListener.
 *
 * The actor comes from the {@link CurrentUser} principal when the request was
 * authenticated by the application's converters (exact user id, no lookup);
 * a plain JWT or any other authenticated principal is described by name and
 * subject so the listener can still resolve it.
 *
 * Log format:
 *   [AUDIT] actor=<user> action=<ACTION> method=<Class.method> duration=<Xms> status=OK
 *   [AUDIT] actor=<user> action=<ACTION> method=<Class.method> duration=<Xms> status=ERROR: <message>
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)   // inside the transaction advisor: the event is published while the tx is open (ADR 0007)
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;

    /** Who is acting: display name, identity-provider subject (nullable), backend user id (nullable). */
    record Actor(String name, String subject, UUID userId) {
    }

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        long start = System.currentTimeMillis();

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        Class<?> targetClass = pjp.getTarget().getClass();
        String methodName = targetClass.getSimpleName() + "." + method.getName();
        String action = resolveAction(audited, method);
        Actor actor = resolveActor();

        try {
            Object result = pjp.proceed();
            long ms = System.currentTimeMillis() - start;

            if (audited.description().isBlank()) {
                log.info("[AUDIT] actor={} action={} method={} duration={}ms status=OK", actor.name(), action, methodName, ms);
            } else {
                log.info("[AUDIT] actor={} action={} method={} duration={}ms status=OK desc={}", actor.name(), action, methodName, ms, audited.description());
            }

            publishEvent(targetClass, audited, action, actor, pjp.getArgs(), result);
            return result;

        } catch (Throwable ex) {
            long ms = System.currentTimeMillis() - start;
            log.warn("[AUDIT] actor={} action={} method={} duration={}ms status=ERROR: {}", actor.name(), action, methodName, ms, ex.getMessage());
            throw ex;
        }
    }

    private void publishEvent(Class<?> targetClass, Audited audited, String action, Actor actor, Object[] args, Object result) {
        if (targetClass.getSimpleName().equals("AuditLogService")) {
            // Avoid self-referential recursion: AuditLogService.record() is itself @Audited.
            return;
        }
        UUID entityId = resolveEntityId(args, result);
        if (entityId == null) {
            return;
        }
        String entityType = resolveEntityType(audited, targetClass);
        eventPublisher.publishEvent(new AuditEvent(actor.name(), actor.subject(), actor.userId(),
                action, entityType, entityId, Instant.now()));
    }

    private UUID resolveEntityId(Object[] args, Object result) {
        if (result != null) {
            try {
                Method idAccessor = result.getClass().getMethod("id");
                Object id = idAccessor.invoke(result);
                if (id instanceof UUID uuid) {
                    return uuid;
                }
            } catch (ReflectiveOperationException ignored) {
                // result has no id() accessor (e.g. void delete methods) -- fall through to args
            }
        }
        for (Object arg : args) {
            if (arg instanceof UUID uuid) {
                return uuid;
            }
        }
        return null;
    }

    private String resolveEntityType(Audited audited, Class<?> targetClass) {
        if (!audited.entityType().isBlank()) {
            return audited.entityType();
        }
        String simpleName = targetClass.getSimpleName();
        String stripped = simpleName.endsWith("Service") ? simpleName.substring(0, simpleName.length() - "Service".length()) : simpleName;
        return stripped.isEmpty() ? simpleName.toLowerCase() : Character.toLowerCase(stripped.charAt(0)) + stripped.substring(1);
    }

    private String resolveAction(Audited audited, Method method) {
        if (!audited.action().isBlank()) return audited.action();
        // Convert camelCase method name to UPPER_SNAKE_CASE: createItem → CREATE_ITEM
        return method.getName()
            .replaceAll("([a-z])([A-Z])", "$1_$2")
            .toUpperCase();
    }

    /**
     * Resolution order: the application's {@link CurrentUser} principal (exact,
     * what both authentication converters produce), then any other authenticated
     * name, then the correlation id or "system" for unauthenticated work such as jobs.
     */
    private Actor resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<CurrentUser> current = CurrentUser.from(auth);
        if (current.isPresent()) {
            CurrentUser user = current.get();
            return new Actor(user.username(), user.subject(), user.userId());
        }
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return new Actor(auth.getName(), null, null);
        }
        String cid = MDC.get("correlationId");
        return new Actor(cid != null ? "cid:" + cid : "system", null, null);
    }
}
