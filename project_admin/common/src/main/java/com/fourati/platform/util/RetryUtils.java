package com.fourati.platform.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Simple retry utility without any external dependency.
 * Use for flaky external calls (HTTP, email send, file I/O) where a brief pause fixes things.
 *
 * For production-grade retry with backoff consider Spring Retry or Resilience4j.
 */
public final class RetryUtils {

    private static final Logger log = LoggerFactory.getLogger(RetryUtils.class);

    private RetryUtils() {}

    /**
     * Retries the given supplier up to maxAttempts times with a fixed delay between attempts.
     * Returns the first successful result.
     * Throws the last exception if all attempts fail.
     *
     * <pre>
     * String result = RetryUtils.retry(3, 500, () -> externalApi.call());
     * </pre>
     */
    public static <T> T retry(int maxAttempts, long delayMs, Supplier<T> action) {
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (Exception e) {
                last = e;
                log.warn("Attempt {}/{} failed: {}", attempt, maxAttempts, e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        throw new RuntimeException("All " + maxAttempts + " attempts failed. Last error: " + last.getMessage(), last);
    }

    /**
     * Retries with exponential backoff: delay doubles on each attempt.
     * Attempt 1: wait initialDelayMs, attempt 2: wait 2×, attempt 3: wait 4×, ...
     */
    public static <T> T retryWithBackoff(int maxAttempts, long initialDelayMs, Supplier<T> action) {
        Exception last = null;
        long delay = initialDelayMs;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (Exception e) {
                last = e;
                log.warn("Attempt {}/{} failed (next in {}ms): {}", attempt, maxAttempts, delay, e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delay);
                        delay *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        throw new RuntimeException("All " + maxAttempts + " attempts failed. Last error: " + last.getMessage(), last);
    }

    /**
     * Fire-and-forget variant — retries silently, returns defaultValue if all fail.
     */
    public static <T> T retryOrDefault(int maxAttempts, long delayMs, Supplier<T> action, T defaultValue) {
		try {
			return retry(maxAttempts, delayMs, action);
		} catch (Exception e) {
			log.error("All retry attempts exhausted, returning default: {}", e.getMessage());
			return defaultValue;
		}
    }
}
