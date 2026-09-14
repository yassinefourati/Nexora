package com.fourati.shared.persistence;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Client-side optimistic locking. Update requests may carry the {@code version}
 * they read; if the row has moved on since, the update is refused with 409
 * {@code CONCURRENT_MODIFICATION} instead of silently overwriting someone
 * else's change. A request without a version is applied unconditionally
 * (server-side {@code @Version} still protects overlapping transactions).
 */
public final class VersionCheck {

    private VersionCheck() {
    }

    public static void check(Class<?> entityType, Object id, long currentVersion, Long expectedVersion) {
        if (expectedVersion != null && expectedVersion != currentVersion) {
            throw new ObjectOptimisticLockingFailureException(entityType, id);
        }
    }
}
