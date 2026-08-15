package com.fourati.platform.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuditAspect's @Around advice, exercised directly (no AspectJ weaving
 * needed) with a mocked ProceedingJoinPoint. Covers actor resolution from
 * SecurityContextHolder, the "system" fallback, and the AuditLogService recursion guard.
 */
@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ProceedingJoinPoint pjp;

    @Mock
    private MethodSignature signature;

    private AuditAspect auditAspect;

    /** Local fake response type mirroring the XxxResponse records' id() accessor. */
    private record FakeResponse(UUID id) {
    }

    private static class FakeTarget {
        FakeResponse create() {
            return null;
        }
    }

    private static class AuditLogService {
        FakeResponse record() {
            return null;
        }
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Audited auditedAnnotationFor(String action) {
        return new Audited() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Audited.class;
            }

            @Override
            public String action() {
                return action;
            }

            @Override
            public String description() {
                return "";
            }

            @Override
            public String entityType() {
                return "";
            }
        };
    }

    @Test
    void audit_publishesEventWithAuthenticatedActorName() throws Throwable {
        auditAspect = new AuditAspect(eventPublisher);

        var authentication = new UsernamePasswordAuthenticationToken("alice", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UUID entityId = UUID.randomUUID();
        FakeResponse result = new FakeResponse(entityId);
        FakeTarget target = new FakeTarget();
        Method method = FakeTarget.class.getDeclaredMethod("create");

        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(pjp.getTarget()).thenReturn(target);
        when(pjp.proceed()).thenReturn(result);
        when(pjp.getArgs()).thenReturn(new Object[0]);

        Audited audited = auditedAnnotationFor("CREATE");

        Object returned = auditAspect.audit(pjp, audited);

        assertThat(returned).isSameAs(result);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        AuditEvent published = captor.getValue();

        assertThat(published.actor()).isEqualTo("alice");
        assertThat(published.action()).isEqualTo("CREATE");
        assertThat(published.entityId()).isEqualTo(entityId);
    }

    @Test
    void audit_fallsBackToSystemActor_whenUnauthenticated() throws Throwable {
        auditAspect = new AuditAspect(eventPublisher);

        // No authentication set on the SecurityContext, and no correlationId in MDC.
        SecurityContextHolder.clearContext();
        org.slf4j.MDC.remove("correlationId");

        UUID entityId = UUID.randomUUID();
        FakeResponse result = new FakeResponse(entityId);
        FakeTarget target = new FakeTarget();
        Method method = FakeTarget.class.getDeclaredMethod("create");

        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(pjp.getTarget()).thenReturn(target);
        when(pjp.proceed()).thenReturn(result);
        when(pjp.getArgs()).thenReturn(new Object[0]);

        Audited audited = auditedAnnotationFor("CREATE");

        auditAspect.audit(pjp, audited);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().actor()).isEqualTo("system");
    }

    @Test
    void audit_doesNotPublish_whenTargetIsAuditLogService() throws Throwable {
        auditAspect = new AuditAspect(eventPublisher);

        var authentication = new UsernamePasswordAuthenticationToken("alice", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        FakeResponse result = new FakeResponse(UUID.randomUUID());
        AuditLogService target = new AuditLogService();
        Method method = AuditLogService.class.getDeclaredMethod("record");

        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(pjp.getTarget()).thenReturn(target);
        when(pjp.proceed()).thenReturn(result);

        Audited audited = auditedAnnotationFor("CREATE");

        auditAspect.audit(pjp, audited);

        verify(eventPublisher, never()).publishEvent(any());
    }
}
