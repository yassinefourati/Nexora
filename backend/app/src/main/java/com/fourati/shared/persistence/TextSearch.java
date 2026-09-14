package com.fourati.shared.persistence;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;

/**
 * Case-insensitive "contains" search over a few string columns, used by every
 * list endpoint's {@code ?q=} parameter. A blank query matches everything, so
 * callers can pass the raw request parameter straight through.
 */
public final class TextSearch {

    private TextSearch() {
    }

    public static <T> Specification<T> contains(String q, String... fields) {
        if (q == null || q.isBlank() || fields.length == 0) {
            return (root, query, cb) -> cb.conjunction();
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(Arrays.stream(fields)
                .map(f -> (Predicate) cb.like(cb.lower(cb.coalesce(root.get(f).as(String.class), "")), like))
                .toArray(Predicate[]::new));
    }
}
