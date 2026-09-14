package com.fourati.platform.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Adds the deprecation headers for handlers annotated with {@link SunsetEndpoint}.
 * Runs before the handler so the headers are present on every response,
 * including errors.
 */
public class SunsetHeadersInterceptor implements HandlerInterceptor {

    public static final String DEPRECATION = "Deprecation";
    public static final String SUNSET = "Sunset";
    public static final String LINK = "Link";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod method) {
            SunsetEndpoint sunset = method.getMethodAnnotation(SunsetEndpoint.class);
            if (sunset != null) {
                response.setHeader(DEPRECATION, "@" + LocalDate.parse(sunset.since()).atStartOfDay(ZoneOffset.UTC).toEpochSecond());
                response.setHeader(SUNSET, DateTimeFormatter.RFC_1123_DATE_TIME
                        .format(LocalDate.parse(sunset.sunset()).atStartOfDay(ZoneOffset.UTC)));
                response.setHeader(LINK, "<" + sunset.successor() + ">; rel=\"successor-version\"");
            }
        }
        return true;
    }
}
