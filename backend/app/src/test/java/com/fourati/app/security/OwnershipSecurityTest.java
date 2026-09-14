package com.fourati.app.security;

import com.fourati.app.security.OwnershipSecurity;

import com.fourati.platform.security.CurrentUser;
import com.fourati.content.ContentFacade;
import com.fourati.notification.NotificationFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers the ownership checks backing the
 * {@code hasRole('ADMIN') or @ownership.*(#id, authentication)} expressions on
 * user-notification, comment and attachment endpoints. Without these, any
 * authenticated user could read, mark-read or delete rows belonging to any
 * other user (IDOR). Identity is the {@link CurrentUser} principal resolved at
 * authentication, so the checks are keyed on the backend user id, never on a
 * claim string, and never on an API key.
 */
@ExtendWith(MockitoExtension.class)
class OwnershipSecurityTest {

    @Mock private NotificationFacade notificationFacade;
    @Mock private ContentFacade contentFacade;

    @InjectMocks
    private OwnershipSecurity ownership;

    private static final UUID ME = UUID.randomUUID();

    private static Authentication person(UUID userId) {
        return new UsernamePasswordAuthenticationToken(new CurrentUser(userId, "sub-alice", "alice", false), null, List.of());
    }

    private static Authentication apiKeyOf(UUID userId) {
        return new UsernamePasswordAuthenticationToken(new CurrentUser(userId, null, "alice", true), null, List.of());
    }

    @Test
    void isSelf_matchesCallersOwnBackendRow() {
        var auth = person(ME);

        assertThat(ownership.isSelf(ME, auth)).isTrue();
        assertThat(ownership.isSelf(UUID.randomUUID(), auth)).isFalse();
    }

    @Test
    void isSelf_nullUserId_isFalse() {
        assertThat(ownership.isSelf(null, person(ME))).isFalse();
    }

    @Test
    void isNotificationOwner_otherUsersRow_isFalse() {
        UUID id = UUID.randomUUID();
        when(notificationFacade.isNotificationOwner(id, ME)).thenReturn(false);

        assertThat(ownership.isNotificationOwner(id, person(ME))).isFalse();
    }

    @Test
    void isCommentAuthor_ownRow_isTrue() {
        UUID id = UUID.randomUUID();
        when(contentFacade.isCommentAuthor(id, ME)).thenReturn(true);

        assertThat(ownership.isCommentAuthor(id, person(ME))).isTrue();
    }

    @Test
    void isAttachmentUploader_ownRow_isTrue() {
        UUID id = UUID.randomUUID();
        when(contentFacade.isAttachmentUploader(id, ME)).thenReturn(true);

        assertThat(ownership.isAttachmentUploader(id, person(ME))).isTrue();
    }

    @Test
    void principalWithoutCurrentUser_isAlwaysFalse_andNeverQueriesRows() {
        var auth = new UsernamePasswordAuthenticationToken("someone", "password");
        UUID id = UUID.randomUUID();

        assertThat(ownership.isSelf(id, auth)).isFalse();
        assertThat(ownership.isNotificationOwner(id, auth)).isFalse();
        assertThat(ownership.isCommentAuthor(id, auth)).isFalse();
        assertThat(ownership.isAttachmentUploader(id, auth)).isFalse();
        verifyNoInteractions(notificationFacade, contentFacade);
    }

    @Test
    void apiKeyCaller_neverOwnsAnything_evenItsOwnersRows() {
        var auth = apiKeyOf(ME);
        UUID id = UUID.randomUUID();

        assertThat(ownership.isSelf(ME, auth)).isFalse();
        assertThat(ownership.isNotificationOwner(id, auth)).isFalse();
        assertThat(ownership.isCommentAuthor(id, auth)).isFalse();
        verifyNoInteractions(notificationFacade, contentFacade);
    }

    @Test
    void unprovisionedPerson_isDenied_ratherThanCrashing() {
        var auth = person(null);
        UUID id = UUID.randomUUID();

        assertThat(ownership.isSelf(id, auth)).isFalse();
        assertThat(ownership.isCommentAuthor(id, auth)).isFalse();
        verifyNoInteractions(contentFacade);
    }
}
