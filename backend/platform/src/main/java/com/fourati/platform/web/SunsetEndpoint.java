package com.fourati.platform.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an endpoint that is kept for compatibility and will be removed.
 * {@link SunsetHeadersInterceptor} turns it into the standard RFC 9745
 * {@code Deprecation}, RFC 8594 {@code Sunset} and {@code Link rel="successor-version"}
 * response headers, so clients and API tooling can see the retirement plan
 * without reading release notes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SunsetEndpoint {

    /** ISO date the endpoint was deprecated, for example {@code 2026-09-13}. */
    String since();

    /** ISO date after which the endpoint may stop responding, for example {@code 2027-03-31}. */
    String sunset();

    /** Path (or URI template) of the endpoint to use instead. */
    String successor();
}
