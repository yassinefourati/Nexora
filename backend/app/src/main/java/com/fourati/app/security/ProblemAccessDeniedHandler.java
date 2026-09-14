package com.fourati.app.security;

import com.fourati.platform.error.ErrorCode;
import com.fourati.platform.error.Problems;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 403 as an RFC 9457 problem-details body (ADR 0005) for denials raised in the
 * filter chain. Denials raised by {@code @PreAuthorize} inside a controller are
 * handled by {@code GlobalExceptionHandler} with the same shape.
 */
@Component
@RequiredArgsConstructor
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

    private final Problems problems;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        problems.write(response, problems.of(ErrorCode.FORBIDDEN,
                "You do not have permission to perform this action.", request.getRequestURI()));
    }
}
