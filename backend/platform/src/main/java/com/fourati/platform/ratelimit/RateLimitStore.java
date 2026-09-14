package com.fourati.platform.ratelimit;

/**
 * Token-bucket state shared by every replica of the API. One bucket per key,
 * refilled at {@code rpm} tokens per minute with a burst capacity of one
 * minute's worth, so an idle client cannot bank hours of credit.
 */
public interface RateLimitStore {

    /** Consumes one token from {@code key}'s bucket; false when the bucket is empty. */
    boolean tryConsume(String key, int rpm);
}
