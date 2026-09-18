package com.fourati.service;

import com.fourati.domain.UnderwritingCase;
import com.fourati.domain.UnderwritingNote;
import com.fourati.dto.request.CreateUnderwritingNoteRequest;
import com.fourati.dto.response.UnderwritingNoteResponse;
import com.fourati.mapper.UnderwritingNoteMapper;
import com.fourati.repository.UnderwritingCaseRepository;
import com.fourati.repository.UnderwritingNoteRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UnderwritingNoteService {

    private final UnderwritingNoteRepository underwritingNoteRepository;
    private final UnderwritingCaseRepository underwritingCaseRepository;
    private final UnderwritingNoteMapper underwritingNoteMapper;

    @Audited(action = "CREATE", description = "Add a note to an underwriting case")
    public UnderwritingNoteResponse create(CreateUnderwritingNoteRequest request) {
        UnderwritingCase underwritingCase = underwritingCaseRepository.findById(request.underwritingCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("UnderwritingCase", request.underwritingCaseId()));

        UnderwritingNote entity = underwritingNoteMapper.toEntity(request);
        entity.setUnderwritingCase(underwritingCase);
        UnderwritingNote saved = underwritingNoteRepository.save(entity);
        return underwritingNoteMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UnderwritingNoteResponse> findByUnderwritingCaseId(UUID underwritingCaseId) {
        return underwritingNoteRepository.findByUnderwritingCaseIdOrderByCreatedAtAsc(underwritingCaseId).stream()
                .map(underwritingNoteMapper::toResponse)
                .toList();
    }
}
