package com.fourati.platform.audit;

import com.fourati.platform.security.CurrentUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuditAspect's @Around advice, exercised directly (no AspectJ weaving
 * needed) with a mocked ProceedingJoinPoint. Covers actor resolution from the
 * {@link CurrentUser} principal, from a plain authenticated name, the "system"
 * fallback, and the AuditLogService recursion guard.
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

    @BeforeEach
    void setUp() {
        auditAspect = new AuditAspect(eventPublisher);
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

    private FakeResponse stubCreateCall() throws Throwable {
        FakeResponse result = new FakeResponse(UUID.randomUUID());
        FakeTarget target = new FakeTarget();
        Method method = FakeTarget.class.getDeclaredMethod("create");
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(pjp.getTarget()).thenReturn(target);
        when(pjp.proceed()).thenReturn(result);
        when(pjp.getArgs()).thenReturn(new Object[0]);
        return result;
    }

    private AuditEvent publishedEvent() {
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }

    @Test
    void audit_usesTheCurrentUserPrincipal_forNameSubjectAndUserId() throws Throwable {
        UUID userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentUser(userId, "sub-alice", "alice", false), null, List.of()));
        FakeResponse result = stubCreateCall();

        Object returned = auditAspect.audit(pjp, auditedAnnotationFor("CREATE"));

        assertThat(returned).isSameAs(result);
        AuditEvent published = publishedEvent();
        assertThat(published.actor()).isEqualTo("alice");
        assertThat(published.actorSubject()).isEqualTo("sub-alice");
        assertThat(published.actorUserId()).isEqualTo(userId);
        assertThat(published.action()).isEqualTo("CREATE");
        assertThat(published.entityId()).isEqualTo(result.id());
    }

    @Test
    void audit_fallsBackToAuthenticatedName_forOtherPrincipals() throws Throwable {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null, List.of()));
        stubCreateCall();

        auditAspect.audit(pjp, auditedAnnotationFor("CREATE"));

        AuditEvent published = publishedEvent();
        assertThat(published.actor()).isEqualTo("alice");
        assertThat(published.actorSubject()).isNull();
        assertThat(published.actorUserId()).isNull();
    }

    @Test
    void audit_fallsBackToSystemActor_whenUnauthenticated() throws Throwable {
        SecurityContextHolder.clearContext();
        org.slf4j.MDC.remove("correlationId");
        stubCreateCall();

        auditAspect.audit(pjp, auditedAnnotationFor("CREATE"));

        assertThat(publishedEvent().actor()).isEqualTo("system");
    }

    @Test
    void audit_doesNotPublish_whenTargetIsAuditLogService() throws Throwable {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null, List.of()));

        FakeResponse result = new FakeResponse(UUID.randomUUID());
        AuditLogService target = new AuditLogService();
        Method method = AuditLogService.class.getDeclaredMethod("record");
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(pjp.getTarget()).thenReturn(target);
        when(pjp.proceed()).thenReturn(result);

        auditAspect.audit(pjp, auditedAnnotationFor("CREATE"));

        verify(eventPublisher, never()).publishEvent(any());
    }
}
