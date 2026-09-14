package com.fourati.app.security;

import com.fourati.content.ContentFacade;
import com.fourati.notification.NotificationFacade;
import com.fourati.platform.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Ownership checks for user-scoped resources, used from {@code @PreAuthorize}
 * expressions alongside {@code hasRole('ADMIN')} so a non-admin caller can only
 * act on rows that belong to them. The caller's identity is the
 * {@link CurrentUser} principal resolved once at authentication (ADR 0006):
 * no database lookup happens here, and an API-key caller never passes, so a
 * key can act on its owner's rows only through scope-guarded endpoints.
 *
 * Questions about another domain's rows go through that domain's facade
 * ({@link ContentFacade}, {@link NotificationFacade}); this bean never touches
 * another module's repositories (ADR 0004).
 */
@Component("ownership")
@RequiredArgsConstructor
public class OwnershipSecurity {

    private final ContentFacade contentFacade;
    private final NotificationFacade notificationFacade;

    /** True when {@code userId} is the caller's own backend user row. */
    public boolean isSelf(UUID userId, Authentication authentication) {
        return userId != null && caller(authentication).map(userId::equals).orElse(false);
    }

    public boolean isNotificationOwner(UUID userNotificationId, Authentication authentication) {
        return caller(authentication)
                .map(me -> notificationFacade.isNotificationOwner(userNotificationId, me))
                .orElse(false);
    }

    public boolean isCommentAuthor(UUID commentId, Authentication authentication) {
        return caller(authentication)
                .map(me -> contentFacade.isCommentAuthor(commentId, me))
                .orElse(false);
    }

    public boolean isAttachmentUploader(UUID attachmentId, Authentication authentication) {
        return caller(authentication)
                .map(me -> contentFacade.isAttachmentUploader(attachmentId, me))
                .orElse(false);
    }

    private Optional<UUID> caller(Authentication authentication) {
        return CurrentUser.personId(authentication);
    }
}
