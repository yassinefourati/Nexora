package com.fourati.integration;

import com.fourati.identity.IdentityFacade;

import com.fourati.access.AccessFacade;

import com.fourati.access.AccessChanged;
import com.fourati.identity.IdentityChanged;
import com.fourati.platform.idempotency.IdempotencyStore;
import com.fourati.platform.idempotency.RedisIdempotencyStore;
import com.fourati.platform.ratelimit.RateLimitStore;
import com.fourati.platform.ratelimit.RedisRateLimitStore;
import com.fourati.identity.internal.persistence.UserIdentityProviderRepository;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.access.internal.application.RolePermissionService;
import com.fourati.identity.internal.application.UserProvisioningService;
import com.fourati.identity.ProvisionedUser;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ADR 0008: state that must be correct across replicas lives in shared
 * stores, invalidation is event-driven and happens after commit, and
 * scheduled jobs are mutually exclusive.
 *
 * Every replica of a multi-replica deployment shares the Redis these tests
 * run against, so "the key is gone from Redis" is exactly "every replica sees
 * the invalidation".
 */
class DistributedStateIntegrationTest extends AbstractIntegrationTest {

    private static final String EFFECTIVE_PERMISSIONS_KEYS = "admin-api:cache:" + AccessFacade.EFFECTIVE_PERMISSIONS_CACHE + "*";
    private static final String PROVISIONED_USERS_KEYS = "admin-api:cache:" + IdentityFacade.PROVISIONED_USERS_CACHE + "*";

    @Autowired private CacheManager cacheManager;
    @Autowired private RateLimitStore rateLimitStore;
    @Autowired private IdempotencyStore idempotencyStore;
    @Autowired private StringRedisTemplate redis;
    @Autowired private RolePermissionService rolePermissionService;
    @Autowired private UserProvisioningService userProvisioningService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserIdentityProviderRepository identityProviderRepository;
    @Autowired private ApplicationEventPublisher events;
    @Autowired private TransactionTemplate tx;
    @Autowired private LockProvider lockProvider;
    @Autowired private JdbcTemplate jdbc;

    private Set<String> keys(String pattern) {
        return redis.keys(pattern);
    }

    @Test
    void cacheAndSharedStores_areRedisBacked_underTheSharedStoreSetting() {
        assertThat(cacheManager).isInstanceOf(RedisCacheManager.class);
        assertThat(rateLimitStore).isInstanceOf(RedisRateLimitStore.class);
        assertThat(idempotencyStore).isInstanceOf(RedisIdempotencyStore.class);
    }

    @Test
    void accessChange_clearsEffectivePermissions_inTheSharedStore_afterCommit() {
        rolePermissionService.findEffectivePermissionsByRoleNames(List.of("admin"));
        assertThat(keys(EFFECTIVE_PERMISSIONS_KEYS)).as("cache warmed in Redis").isNotEmpty();

        tx.executeWithoutResult(status -> {
            events.publishEvent(new AccessChanged(null, UUID.randomUUID(), "TEST"));
            // Still cached while the transaction is open: a concurrent reader may not observe a
            // clear for data that has not been committed yet.
            assertThat(keys(EFFECTIVE_PERMISSIONS_KEYS)).as("not cleared before commit").isNotEmpty();
        });

        assertThat(keys(EFFECTIVE_PERMISSIONS_KEYS)).as("cleared after commit").isEmpty();
    }

    @Test
    void identityChange_clearsProvisionedUsers_inTheSharedStore() {
        String subject = "sub-cache-" + UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").subject(subject)
                .claim("preferred_username", "cache-" + UUID.randomUUID())
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        ProvisionedUser provisioned = userProvisioningService.ensureUser(jwt);
        try {
            assertThat(keys(PROVISIONED_USERS_KEYS)).as("provisioning result cached in Redis").isNotEmpty();

            tx.executeWithoutResult(status -> events.publishEvent(new IdentityChanged(provisioned.id(), "TEST")));

            assertThat(keys(PROVISIONED_USERS_KEYS)).as("cleared after commit").isEmpty();
        } finally {
            identityProviderRepository.findByProviderAndProviderUserId(UserProvisioningService.PROVIDER, subject)
                    .ifPresent(identityProviderRepository::delete);
            userRepository.deleteById(provisioned.id());
        }
    }

    @Test
    void schedulerLock_tableExists_andOnlyOneRunnerHoldsALock() {
        assertThat(jdbc.queryForObject("select count(*) from shedlock", Integer.class)).isNotNull();

        LockingTaskExecutor replicaA = new DefaultLockingTaskExecutor(lockProvider);
        LockingTaskExecutor replicaB = new DefaultLockingTaskExecutor(lockProvider);
        LockConfiguration lock = new LockConfiguration(Instant.now(), "integration-test-lock",
                Duration.ofSeconds(30), Duration.ZERO);
        AtomicInteger runs = new AtomicInteger();

        replicaA.executeWithLock((Runnable) () -> {
            runs.incrementAndGet();
            // Another replica (another thread, another executor) asking for the same lock while
            // it is held must be skipped, not run.
            CompletableFuture.runAsync(() -> replicaB.executeWithLock((Runnable) runs::incrementAndGet, lock)).join();
        }, lock);

        assertThat(runs.get()).isEqualTo(1);
    }
}
