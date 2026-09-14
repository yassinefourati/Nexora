package com.fourati.app.cache;

import com.fourati.access.AccessChanged;
import com.fourati.access.AccessFacade;
import com.fourati.identity.IdentityChanged;
import com.fourati.identity.IdentityFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * The one place caches are invalidated (ADR 0008). Domain modules publish what
 * changed; this listener decides what to forget, after the change is
 * committed so a concurrent reader cannot repopulate the cache with
 * pre-commit data.
 *
 * With the Redis-backed store (every profile that runs more than one replica)
 * the {@link CacheInvalidator} deletes the shared keys, so all replicas see
 * the invalidation at once. With the in-memory store it clears the local
 * cache, which is the whole cache.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationListener {

    private final CacheInvalidator cacheInvalidator;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAccessChanged(AccessChanged event) {
        clear(AccessFacade.EFFECTIVE_PERMISSIONS_CACHE, event.reason());
        clear(AccessFacade.USER_PERMISSIONS_CACHE, event.reason());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onIdentityChanged(IdentityChanged event) {
        clear(IdentityFacade.PROVISIONED_USERS_CACHE, event.reason());
    }

    private void clear(String cacheName, String reason) {
        cacheInvalidator.clear(cacheName);
        log.debug("Cache '{}' cleared ({})", cacheName, reason);
    }
}
