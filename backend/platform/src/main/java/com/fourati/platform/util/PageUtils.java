package com.fourati.platform.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;

/**
 * Pagination utilities.
 */
public final class PageUtils {

    private PageUtils() {}

    /**
     * Wraps a plain List into a Spring Data Page — useful when your data source
     * doesn't natively support Pageable (e.g. external API, in-memory list).
     */
    public static <T> Page<T> toPage(List<T> all, Pageable pageable) {
		if (all == null || all.isEmpty())
			return Page.empty(pageable);
		int start = (int) pageable.getOffset();
		if (start >= all.size())
			return new PageImpl<>(List.of(), pageable, all.size());
		int end = Math.min(start + pageable.getPageSize(), all.size());
		return new PageImpl<>(all.subList(start, end), pageable, all.size());
    }

    /**
     * Maps a Page<Entity> to Page<Dto> using a mapper function.
     * Shorthand for page.map(mapper).
     */
    public static <T, R> Page<R> map(Page<T> page, Function<T, R> mapper) {
        return page.map(mapper);
    }

    /** Creates a Pageable for the first page with the given size. */
    public static Pageable firstPage(int size) {
        return PageRequest.of(0, size);
    }

    /** Returns true when the page is the last one. */
    public static boolean isLastPage(Page<?> page) {
        return !page.hasNext();
    }

    /**
     * Safely clamps the page size to avoid accidental huge queries.
     * Uses the requested size, but never exceeds maxSize.
     */
	public static Pageable clampPageSize(Pageable pageable, int maxSize) {
		if (pageable.getPageSize() <= maxSize)
			return pageable;
		return PageRequest.of(pageable.getPageNumber(), maxSize, pageable.getSort());
    }
}
