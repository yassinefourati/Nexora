package com.fourati.content;

import java.util.UUID;

/**
 * What other modules may ask the content domain. Ownership questions are
 * answered here so the identity module's authorization beans never touch
 * content's repositories (ADR 0004: modules talk through facades or events).
 */
public interface ContentFacade {

    /** True when the comment exists and was written by {@code userId}. */
    boolean isCommentAuthor(UUID commentId, UUID userId);

    /** True when the attachment exists and was uploaded by {@code userId}. */
    boolean isAttachmentUploader(UUID attachmentId, UUID userId);
}
