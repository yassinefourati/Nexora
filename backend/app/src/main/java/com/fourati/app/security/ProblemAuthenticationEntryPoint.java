package com.fourati.app.security;

import com.fourati.platform.error.ErrorCode;
import com.fourati.platform.error.Problems;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 401 as an RFC 9457 problem-details body (ADR 0005), replacing Spring
 * Security's empty-body default. Keeps the {@code WWW-Authenticate: Bearer}
 * challenge, with {@code error} and {@code error_description} when the
 * failure came from an invalid or expired token, so RFC 6750 clients still
 * get what they expect.
 */
@Component
@RequiredArgsConstructor
public class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Problems problems;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String detail = "Authentication is required. Provide a valid Bearer token or X-Api-Key.";
        StringBuilder challenge = new StringBuilder("Bearer");
        if (authException instanceof OAuth2AuthenticationException oauth) {
            OAuth2Error error = oauth.getError();
            if (error != null) {
                challenge.append(" error=\"").append(quote(error.getErrorCode())).append('"');
                if (error.getDescription() != null) {
                    challenge.append(", error_description=\"").append(quote(error.getDescription())).append('"');
                    detail = error.getDescription();
                }
            }
        }
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, challenge.toString());
        problems.write(response, problems.of(ErrorCode.UNAUTHORIZED, detail, request.getRequestURI()));
    }

    private static String quote(String value) {
        return value == null ? "" : value.replace('"', '\'');
    }
}
