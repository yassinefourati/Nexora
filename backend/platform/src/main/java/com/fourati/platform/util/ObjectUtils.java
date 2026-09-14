package com.fourati.platform.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Null-safe object utilities.
 */
public final class ObjectUtils {

    private ObjectUtils() {}

    /**
     * Null-safe property accessor — avoids NPE chaining.
     * mapOrNull(order, o -> o.getCustomer().getEmail())
     * Returns null if order or any intermediate value is null.
     */
    public static <T, R> R mapOrNull(T value, Function<T, R> mapper) {
		return value == null ? null : mapper.apply(value);
    }

    /**
     * Null-safe property accessor with default.
     * mapOrDefault(order, o -> o.getCustomer().getEmail(), "unknown")
     */
    public static <T, R> R mapOrDefault(T value, Function<T, R> mapper, R defaultValue) {
		if (value == null)
			return defaultValue;
		R result = mapper.apply(value);
		return result != null ? result : defaultValue;
    }

    /**
     * Calls the consumer only when value is non-null.
     * ifPresent(user.getEmail(), email -> sendWelcome(email))
     */
    public static <T> void ifPresent(T value, Consumer<T> consumer) {
		if (value != null)
			consumer.accept(value);
    }

    /**
     * Returns first non-null value from the given options.
     * coalesce(a, b, c) — same as SQL COALESCE
     */
    @SafeVarargs
    public static <T> T coalesce(T... values) {
		for (T v : values)
			if (v != null)
				return v;
		return null;
    }

    /**
     * Applies the mapper only when value is non-null, otherwise returns defaultValue.
     * Useful when building DTOs from entities that have optional nested objects.
     */
    public static <T, R> R transformOrDefault(T value, Function<T, R> mapper, Supplier<R> defaultSupplier) {
        return value != null ? mapper.apply(value) : defaultSupplier.get();
    }

}
