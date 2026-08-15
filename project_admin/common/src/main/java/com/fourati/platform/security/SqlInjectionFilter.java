package com.fourati.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SQL injection and XSS protection filter.
 *
 * Inspects:
 *   ✔ URL query parameters    (?name=value)
 *   ✔ Path variables          (/api/v1/items/{id})
 *   ✔ Request headers         (any header value)
 *   ✔ Request body            (JSON, form-data)
 *
 * Actions (configurable via app.security.input-validation.action):
 *   BLOCK    → returns 400 Bad Request immediately (default)
 *   LOG_ONLY → logs warning and continues (for auditing without blocking)
 *
 * Configure in application.yml:
 *   app:
 *     security:
 *       input-validation:
 *         enabled: true
 *         action: BLOCK
 *         excluded-paths: /actuator,/v3/api-docs,/swagger-ui
 */
@Component
@Order(0)
@Slf4j
public class SqlInjectionFilter extends OncePerRequestFilter {

    @Value("${app.security.input-validation.enabled:true}")
    private boolean enabled;

    @Value("${app.security.input-validation.action:BLOCK}")
    private String action;

    @Value("${app.security.input-validation.excluded-paths:/actuator,/v3/api-docs,/swagger-ui}")
    private String excludedPaths;

    //  SQL injection patterns 

    private static final List<Pattern> SQL_PATTERNS = List.of(
        // Classic injection: ' OR '1'='1, OR '--  (bare ; removed — too broad, covered by stacked-query below)
        pattern("('.+--)|(--[^\\n]+)"),
        // UNION-based injection
        pattern("\\b(UNION)(\\s)+(SELECT|ALL)\\b"),
        // Boolean-based: AND 1=1, OR 1=1
        pattern("\\b(AND|OR)\\s+\\d+\\s*=\\s*\\d+"),
        // DROP, DELETE, INSERT, UPDATE, EXEC, EXECUTE
        pattern("\\b(DROP|DELETE|INSERT|UPDATE|EXEC|EXECUTE|TRUNCATE|ALTER|CREATE)\\b"),
        // Stored proc / xp_cmdshell
        pattern("\\b(xp_|sp_|sys\\.)"),
        // SQL comments
        pattern("(/\\*[\\s\\S]*?\\*/)|(--[^\\n]*)"),
        // Hex encoding bypass: 0x41
        pattern("0x[0-9a-fA-F]+"),
        // SLEEP, WAITFOR (time-based blind)
        pattern("\\b(SLEEP|WAITFOR\\s+DELAY|BENCHMARK)\\b"),
        // CHAR(), CONVERT(), CAST() with suspicious context
        pattern("\\bCHAR\\s*\\(\\s*\\d+"),
        // Stacked queries: ;SELECT, ;DROP
        pattern(";\\s*(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE)")
    );

    //  XSS patterns 

    private static final List<Pattern> XSS_PATTERNS = List.of(
        // <script> tags
        pattern("<\\s*script[^>]*>"),
        // javascript: protocol
        pattern("javascript\\s*:"),
        // event handlers: onclick=, onload=, onerror=, etc.
        pattern("on\\w+\\s*="),
        // <iframe>, <object>, <embed>
        pattern("<\\s*(iframe|object|embed|form|input|button)[^>]*>"),
        // HTML entities used to bypass: &#60; &#x3C;
        pattern("&#[xX]?[0-9a-fA-F]+;"),
        // expression() — CSS XSS
        pattern("expression\\s*\\("),
        // vbscript:
        pattern("vbscript\\s*:")
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!enabled || isExcluded(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap unconditionally for body-bearing methods so the body can be read without
        // consuming the stream. Reuse an existing wrapper to avoid double-wrapping.
        String method = request.getMethod();
        boolean isBodyMethod = "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
        ContentCachingRequestWrapper cached = (request instanceof ContentCachingRequestWrapper ccw)
                ? ccw
                : (isBodyMethod ? new ContentCachingRequestWrapper(request, 1_048_576) : null);
        HttpServletRequest chainRequest = (cached != null) ? cached : request;

        // 1. Check query parameters
        String threat = checkParameters(request.getParameterMap());

        // 2. Check headers (skip standard headers that may contain tokens)
        if (threat == null) threat = checkHeaders(request);

        // 3. Check request body
        if (threat == null && cached != null) threat = checkBody(cached);

        if (threat != null) {
            String msg = "Potential injection attempt detected from ip={} path={} pattern={}"
                .formatted(getClientIp(request), request.getRequestURI(), threat);

            if ("LOG_ONLY".equalsIgnoreCase(action)) {
                log.warn("[SECURITY] {} — action=LOG_ONLY, continuing", msg);
                chain.doFilter(request, response);
            } else {
                log.warn("[SECURITY] {} — action=BLOCK, returning 400", msg);
                reject(response, request.getRequestURI(), threat);
            }
            return;
        }

        chain.doFilter(chainRequest, response);
    }

    //  Checkers 

    private String checkParameters(Map<String, String[]> params) {
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                String detected = detectThreat(value);
                if (detected != null) return "param[" + entry.getKey() + "] → " + detected;
            }
        }
        return null;
    }

    private String checkHeaders(HttpServletRequest request) {
        // Standard HTTP/browser headers that legitimately contain special characters
        // (semicolons in Accept-Language, quotes in sec-ch-ua, etc.) — never scan these
        List<String> skipHeaders = List.of(
            // Auth & identity
            "authorization", "cookie", "proxy-authorization",
            // Content negotiation
            "accept", "accept-encoding", "accept-language", "accept-charset", "content-type",
            // Client info
            "user-agent", "referer", "origin", "host",
            // Connection
            "connection", "keep-alive", "transfer-encoding", "te",
            // Security / CORS (sec-* headers are browser-generated, not user input)
            "sec-fetch-site", "sec-fetch-mode", "sec-fetch-dest", "sec-fetch-user",
            "sec-ch-ua", "sec-ch-ua-mobile", "sec-ch-ua-platform", "sec-ch-ua-arch",
            "sec-ch-ua-full-version", "sec-ch-ua-full-version-list",
            // Cache
            "cache-control", "pragma", "if-none-match", "if-modified-since",
            // Misc standard
            "content-length", "content-encoding", "upgrade-insecure-requests",
            "x-forwarded-for", "x-forwarded-proto", "x-real-ip",
            "x-correlation-id", "x-idempotency-key"
        );
        var names = request.getHeaderNames();
        if (names == null) return null;
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (skipHeaders.contains(name.toLowerCase())) continue;
            String value = request.getHeader(name);
            String detected = detectThreat(value);
            if (detected != null) return "header[" + name + "] → " + detected;
        }
        return null;
    }

    private String checkBody(ContentCachingRequestWrapper request) throws IOException {
        // Force body read
        request.getInputStream().readAllBytes();
        byte[] bodyBytes = request.getContentAsByteArray();
        if (bodyBytes.length == 0) return null;

        String contentType = request.getContentType();
        if (contentType != null && contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            return null; // skip binary uploads
        }

        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        return detectThreat(body);
    }

    //  Pattern matching 

    private String detectThreat(String input) {
        if (input == null || input.isBlank()) return null;
        // Decode common encoding bypasses before checking
        String decoded = decodeInput(input);

        for (Pattern p : SQL_PATTERNS) {
            if (p.matcher(decoded).find()) return "SQL:" + truncate(p.pattern(), 20);
        }
        for (Pattern p : XSS_PATTERNS) {
            if (p.matcher(decoded).find()) return "XSS:" + truncate(p.pattern(), 20);
        }
        return null;
    }

    /**
     * Decode URL-encoding and HTML-entity bypasses so patterns match:
     *   %27 → '   %3B → ;   %2D%2D → --   &#39; → '
     */
    private String decodeInput(String input) {
        try {
            String decoded = java.net.URLDecoder.decode(input, StandardCharsets.UTF_8);
            decoded = decoded.replaceAll("&#?[xX]?[0-9a-fA-F]+;", " ");
            return decoded;
        } catch (Exception e) {
            return input;
        }
    }

    // Helpers

    private void reject(HttpServletResponse response, String path, String threat) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/problem+json");
        String cid = MDC.get("correlationId");
        String ts  = java.time.Instant.now().toString();
        response.getWriter().write("""
            {
              "status": 400,
              "error": {
                "code": "INJECTION_DETECTED",
                "message": "Request rejected: potentially dangerous input detected",
                "retryable": false
              },
              "request": {
                "id": "%s",
                "path": "%s",
                "timestamp": "%s"
              }
            }""".formatted(cid != null ? cid : "", path, ts));
    }

    private boolean isExcluded(String uri) {
        return Arrays.stream(excludedPaths.split(","))
            .map(String::trim)
            .anyMatch(uri::startsWith);
    }

    private String getClientIp(HttpServletRequest req) {
        String fwd = req.getHeader("X-Forwarded-For");
        return fwd != null && !fwd.isBlank() ? fwd.split(",")[0].trim() : req.getRemoteAddr();
    }

    private static Pattern pattern(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}

