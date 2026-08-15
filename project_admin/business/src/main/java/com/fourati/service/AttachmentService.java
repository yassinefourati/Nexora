package com.fourati.service;

import com.fourati.domain.Attachment;
import com.fourati.domain.User;
import com.fourati.dto.request.CreateAttachmentRequest;
import com.fourati.dto.response.AttachmentResponse;
import com.fourati.mapper.AttachmentMapper;
import com.fourati.repository.AttachmentRepository;
import com.fourati.repository.UserRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final AttachmentMapper attachmentMapper;

    @Audited(action = "CREATE", description = "Upload a new attachment")
    public AttachmentResponse create(CreateAttachmentRequest request) {
        Attachment entity = attachmentMapper.toEntity(request);
        if (request.uploadedBy() != null) {
            User uploadedBy = userRepository.findById(request.uploadedBy())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.uploadedBy()));
            entity.setUploadedBy(uploadedBy);
        }
        Attachment saved = attachmentRepository.save(entity);
        return attachmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AttachmentResponse findById(UUID id) {
        return attachmentMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> findByEntity(String entityType, UUID entityId) {
        return attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(attachmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AttachmentResponse> findByUploadedBy(UUID uploadedById, Pageable pageable) {
        return attachmentRepository.findByUploadedById(uploadedById, pageable).map(attachmentMapper::toResponse);
    }

    @Audited(action = "DELETE", description = "Delete an attachment")
    public void delete(UUID id) {
        Attachment entity = getEntityOrThrow(id);
        attachmentRepository.delete(entity);
    }

    private Attachment getEntityOrThrow(UUID id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", id));
    }
}
