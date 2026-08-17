package com.fourati.service;

import com.fourati.domain.Document;
import com.fourati.dto.request.CreateDocumentRequest;
import com.fourati.dto.request.ReviewDocumentRequest;
import com.fourati.dto.response.DocumentResponse;
import com.fourati.mapper.DocumentMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.DocumentRepository;
import com.fourati.repository.DocumentReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentReviewRepository documentReviewRepository;

    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentService documentService;

    private CreateDocumentRequest newRequest() {
        return new CreateDocumentRequest("identity", "identity", "passport.pdf", "docs/passport.pdf", "application/pdf", 12345L);
    }

    @Test
    void create_savesDocument() {
        CreateDocumentRequest request = newRequest();
        Document entity = new Document();

        when(documentRepository.existsByStorageKey(request.storageKey())).thenReturn(false);
        when(documentMapper.toEntity(request)).thenReturn(entity);
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        when(documentMapper.toResponse(any(Document.class))).thenReturn(
                new DocumentResponse(UUID.randomUUID(), "identity", "identity", "passport.pdf",
                        request.storageKey(), "application/pdf", 12345L, "uploaded", null, null, null));

        DocumentResponse response = documentService.create(request);

        assertThat(response.storageKey()).isEqualTo(request.storageKey());
        verify(documentRepository).save(entity);
    }

    @Test
    void create_throwsConflict_whenStorageKeyAlreadyExists() {
        CreateDocumentRequest request = newRequest();
        when(documentRepository.existsByStorageKey(request.storageKey())).thenReturn(true);

        assertThatThrownBy(() -> documentService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(documentRepository, never()).save(any());
    }

    @Test
    void review_transitionsStatusAndRecordsReview() {
        UUID id = UUID.randomUUID();
        Document entity = new Document();
        entity.setStatus("uploaded");
        when(documentRepository.findById(id)).thenReturn(Optional.of(entity));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        when(documentMapper.toResponse(any(Document.class))).thenReturn(
                new DocumentResponse(id, null, null, null, null, null, null, "verified", null, null, null));

        DocumentResponse response = documentService.review(id, new ReviewDocumentRequest("verified", "looks good"));

        assertThat(response.status()).isEqualTo("verified");
        assertThat(entity.getStatus()).isEqualTo("verified");
        verify(documentReviewRepository).save(any());
    }

    @Test
    void review_throwsConflict_whenAlreadyDecided() {
        UUID id = UUID.randomUUID();
        Document entity = new Document();
        entity.setStatus("verified");
        when(documentRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> documentService.review(id, new ReviewDocumentRequest("rejected", "changed mind")))
                .isInstanceOf(ConflictException.class);

        verify(documentRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
