package com.fourati.platform.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Null-safe collection utilities.
 */
public final class CollectionUtils {

    private CollectionUtils() {}

    /** Returns true if the collection is null or empty. */
    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    /** Returns the collection if non-null, otherwise an empty list. */
    public static <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    /** Null-safe stream → List conversion. */
    public static <T, R> List<R> map(Collection<T> source, Function<T, R> mapper) {
        if (isEmpty(source)) 
        	return Collections.emptyList();
        return source.stream().map(mapper).toList();
    }

    /** Null-safe stream → filtered List. */
    public static <T> List<T> filter(Collection<T> source, Predicate<T> predicate) {
        if (isEmpty(source)) 
        	return Collections.emptyList();
        return source.stream().filter(predicate).toList();
    }

    /**
     * Splits a list into fixed-size batches.
     * Useful for bulk DB inserts or API calls with max-page limits.
     * partition(List.of(1..10), 3) → [[1,2,3],[4,5,6],[7,8,9],[10]]
     */
    public static <T> List<List<T>> partition(List<T> list, int batchSize) {
        if (isEmpty(list))
        	return Collections.emptyList();
        return IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
            .mapToObj(i -> list.subList(
                i * batchSize,
                Math.min((i + 1) * batchSize, list.size())
            ))
            .toList();
    }

    /**
     * Groups a collection by a key — like SQL GROUP BY.
     * groupBy(users, User::getDepartment) → Map<"Engineering", [user1, user2]>
     */
    public static <T, K> Map<K, List<T>> groupBy(Collection<T> source, Function<T, K> keyFn) {
        if (isEmpty(source)) 
        	return Collections.emptyMap();
        return source.stream().collect(Collectors.groupingBy(keyFn));
    }

    /**
     * Converts a collection to a lookup map by a unique key.
     * Throws IllegalStateException if the key is not unique.
     */
    public static <T, K> Map<K, T> indexBy(Collection<T> source, Function<T, K> keyFn) {
        if (isEmpty(source)) 
        	return Collections.emptyMap();
        return source.stream().collect(Collectors.toMap(keyFn, Function.identity()));
    }

    /**
     * Returns the first element matching the predicate, or Optional.empty().
     */
    public static <T> Optional<T> findFirst(Collection<T> source, Predicate<T> predicate) {
		if (isEmpty(source))
			return Optional.empty();
        return source.stream().filter(predicate).findFirst();
    }

    /**
     * Returns an element at the given index safely (no IndexOutOfBoundsException).
     */
    public static <T> Optional<T> safeGet(List<T> list, int index) {
		if (isEmpty(list) || index < 0 || index >= list.size())
			return Optional.empty();
        return Optional.ofNullable(list.get(index));
    }

    /** Flattens a list of lists into a single list. */
    public static <T> List<T> flatten(Collection<? extends Collection<T>> nested) {
		if (isEmpty(nested))
			return Collections.emptyList();
        return nested.stream().filter(Objects::nonNull).flatMap(Collection::stream).toList();
    }

    /** Returns distinct elements preserving order. */
    public static <T> List<T> distinct(Collection<T> source) {
		if (isEmpty(source))
			return Collections.emptyList();
        return source.stream().distinct().toList();
    }

    /** Returns elements that appear in both collections (intersection). */
    public static <T> List<T> intersect(Collection<T> a, Collection<T> b) {
		if (isEmpty(a) || isEmpty(b))
			return Collections.emptyList();
        Set<T> setB = new HashSet<>(b);
        return a.stream().filter(setB::contains).distinct().toList();
    }

    /** Returns elements in 'a' that are NOT in 'b' (difference). */
    public static <T> List<T> difference(Collection<T> a, Collection<T> b) {
		if (isEmpty(a))
			return Collections.emptyList();
		if (isEmpty(b))
			return new ArrayList<>(a);
        Set<T> setB = new HashSet<>(b);
        return a.stream().filter(e -> !setB.contains(e)).toList();
    }
}
