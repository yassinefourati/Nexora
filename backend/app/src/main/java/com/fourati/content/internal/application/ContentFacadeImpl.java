package com.fourati.content.internal.application;

import com.fourati.content.ContentFacade;
import com.fourati.content.internal.persistence.AttachmentRepository;
import com.fourati.content.internal.persistence.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ContentFacadeImpl implements ContentFacade {

    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;

    @Override
    public boolean isCommentAuthor(UUID commentId, UUID userId) {
        return commentId != null && userId != null && commentRepository.existsByIdAndUserId(commentId, userId);
    }

    @Override
    public boolean isAttachmentUploader(UUID attachmentId, UUID userId) {
        return attachmentId != null && userId != null && attachmentRepository.existsByIdAndUploadedBy(attachmentId, userId);
    }
}
