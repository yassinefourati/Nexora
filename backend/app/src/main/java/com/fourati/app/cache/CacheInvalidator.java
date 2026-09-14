package com.fourati.app.cache;

/**
 * Port used by {@link CacheInvalidationListener} to forget a whole cache,
 * synchronously and on every replica. The application wires one adapter per
 * store (ADR 0008): a local one that clears the in-JVM cache, and a Redis one
 * that deletes the cache's keys from the shared store before returning.
 *
 * Why not {@code Cache.clear()}: with the Redis cache manager the clear
 * completes asynchronously, so a reader that races the commit can observe the
 * stale entry, and a test cannot assert the invalidation happened.
 */
public interface CacheInvalidator {

    /** Removes every entry of the named cache. Returns only once the removal is visible to all replicas. */
    void clear(String cacheName);
}
