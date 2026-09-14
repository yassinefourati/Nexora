package com.fourati.audit.internal.application;

import com.fourati.shared.persistence.TextSearch;

import com.fourati.audit.internal.domain.ErrorLog;
import com.fourati.audit.CreateErrorLogRequest;
import com.fourati.audit.ErrorLogResponse;
import com.fourati.audit.internal.persistence.ErrorLogRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for error log entries.
 *
 * error_logs is a read-only-from-the-API resource: entries are written
 * internally by the application (via {@link #record(CreateErrorLogRequest)})
 * whenever an error is captured for observability purposes. No public
 * update/delete operations are exposed.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ErrorLogService {

    private final ErrorLogRepository errorLogRepository;
    private final ErrorLogMapper errorLogMapper;

    @Audited(action = "CREATE", description = "Recorded a new error log entry")
    public ErrorLogResponse record(CreateErrorLogRequest request) {
        ErrorLog errorLog = errorLogMapper.toEntity(request);
        ErrorLog saved = errorLogRepository.save(errorLog);
        return errorLogMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ErrorLogResponse findById(UUID id) {
        ErrorLog errorLog = errorLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ErrorLog", id));
        return errorLogMapper.toResponse(errorLog);
    }

    @Transactional(readOnly = true)
    public Page<ErrorLogResponse> findAll(Pageable pageable) {
        return errorLogRepository.findAll(pageable).map(errorLogMapper::toResponse);
    }

    /** Server-side search over source, errorMessage (case-insensitive contains); blank q lists everything. */
    @Transactional(readOnly = true)
    public Page<ErrorLogResponse> search(String q, Pageable pageable) {
        return errorLogRepository.findAll(TextSearch.<ErrorLog>contains(q, "source", "errorMessage"), pageable).map(errorLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ErrorLogResponse> findBySeverity(String severity, Pageable pageable) {
        return errorLogRepository.findBySeverity(severity, pageable).map(errorLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ErrorLogResponse> findBySource(String source, Pageable pageable) {
        return errorLogRepository.findBySource(source, pageable).map(errorLogMapper::toResponse);
    }
}
