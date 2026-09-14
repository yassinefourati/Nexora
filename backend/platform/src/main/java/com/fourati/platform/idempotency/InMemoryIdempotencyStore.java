package com.fourati.platform.idempotency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Single-JVM store; lost on restart and not shared between replicas. */
@Slf4j
public class InMemoryIdempotencyStore implements IdempotencyStore {

    private record Slot(Entry entry, Instant expiresAt) {
        boolean expired() { return Instant.now().isAfter(expiresAt); }
    }

    private final Map<String, Slot> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Entry> claim(String key, Duration inFlightTtl) {
        Slot marker = new Slot(new InFlight(), Instant.now().plus(inFlightTtl));
        Slot existing = store.putIfAbsent(key, marker);
        if (existing != null && existing.expired()) {
            store.remove(key, existing);
            existing = store.putIfAbsent(key, marker);
        }
        return Optional.ofNullable(existing).map(Slot::entry);
    }

    @Override
    public void complete(String key, CachedResponse response, Duration ttl) {
        store.put(key, new Slot(new Completed(response), Instant.now().plus(ttl)));
    }

    @Override
    public void release(String key) {
        Slot slot = store.get(key);
        if (slot != null && slot.entry() instanceof InFlight) {
            store.remove(key, slot);
        }
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void evictExpired() {
        int before = store.size();
        store.entrySet().removeIf(e -> e.getValue().expired());
        int removed = before - store.size();
        if (removed > 0) log.debug("Idempotency store: evicted {} expired entries", removed);
    }
}
