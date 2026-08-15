package com.fourati.platform.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;

/**
 * AOP aspect that intercepts @Audited methods, logs a structured audit entry,
 * and publishes an {@link AuditEvent} for any interested listener to persist.
 *
 * Deliberately domain-agnostic: this class lives under platform.. and must never
 * depend on this app's own domain/repository code, so persisting the event to a
 * database table is the responsibility of a domain-side @EventListener, not this
 * aspect. See com.fourati.audit.AuditLogEventListener.
 *
 * Log format:
 *   [AUDIT] actor=<user> action=<ACTION> method=<Class.method> duration=<Xms> status=OK
 *   [AUDIT] actor=<user> action=<ACTION> method=<Class.method> duration=<Xms> status=ERROR: <message>
 *
 * Requires spring-boot-starter-aop (included transitively via spring-boot-starter).
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        long start = System.currentTimeMillis();

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        Class<?> targetClass = pjp.getTarget().getClass();
        String methodName = targetClass.getSimpleName() + "." + method.getName();
        String action = resolveAction(audited, method);
        String actor = resolveActor();

        try {
            Object result = pjp.proceed();
            long ms = System.currentTimeMillis() - start;

            if (audited.description().isBlank()) {
                log.info("[AUDIT] actor={} action={} method={} duration={}ms status=OK", actor, action, methodName, ms);
            } else {
                log.info("[AUDIT] actor={} action={} method={} duration={}ms status=OK desc={}", actor, action, methodName, ms, audited.description());
            }

            publishEvent(targetClass, audited, action, actor, pjp.getArgs(), result);
            return result;

        } catch (Throwable ex) {
            long ms = System.currentTimeMillis() - start;
            log.warn("[AUDIT] actor={} action={} method={} duration={}ms status=ERROR: {}", actor, action, methodName, ms, ex.getMessage());
            throw ex;
        }
    }

    private void publishEvent(Class<?> targetClass, Audited audited, String action, String actor, Object[] args, Object result) {
        if (targetClass.getSimpleName().equals("AuditLogService")) {
            // Avoid self-referential recursion: AuditLogService.record() is itself @Audited.
            return;
        }
        UUID entityId = resolveEntityId(args, result);
        if (entityId == null) {
            return;
        }
        String entityType = resolveEntityType(audited, targetClass);
        eventPublisher.publishEvent(new AuditEvent(actor, action, entityType, entityId, Instant.now()));
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
     * Returns the current user's Keycloak username (preferred_username), not the
     * JWT subject. The Keycloak SSO subject and this backend's users.id are
     * independently-provisioned UUIDs -- confirmed live, they never match for
     * the same account -- so AuditLogEventListener resolving the actor to a
     * User row must go by username via UserRepository.findByUsername, exactly
     * like the frontend's own useCurrentBackendUser resolver does. Returning
     * the raw JWT sub here (auth.getName()'s default) let a syntactically-valid
     * but nonexistent UUID slip straight into audit_logs.user_id as an FK,
     * failing every audited write with a foreign-key violation.
     */
    private String resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth && auth.isAuthenticated()) {
            Jwt jwt = jwtAuth.getToken();
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            if (preferredUsername != null) {
                return preferredUsername;
            }
            return jwtAuth.getName();
        }
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        String cid = MDC.get("correlationId");
        return cid != null ? "cid:" + cid : "system";
    }
}
