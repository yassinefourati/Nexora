package com.fourati.platform.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Stamps every request with a correlationId in MDC — visible in every log line.
 * Clients can pass X-Correlation-ID to carry the ID across service boundaries.
 */
@Component
@Order(0)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String HEADER  = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String id = req.getHeader(HEADER);
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        MDC.put(MDC_KEY, id);
        res.setHeader(HEADER, id);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
