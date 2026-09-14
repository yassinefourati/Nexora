package com.fourati.platform;

import com.fourati.platform.audit.AuditAspect;
import com.fourati.platform.error.GlobalExceptionHandler;
import com.fourati.platform.error.Problems;
import com.fourati.platform.events.DomainEventPublisher;
import com.fourati.platform.export.ExportService;
import com.fourati.platform.idempotency.IdempotencyFilter;
import com.fourati.platform.idempotency.IdempotencyStore;
import com.fourati.platform.idempotency.InMemoryIdempotencyStore;
import com.fourati.platform.idempotency.RedisIdempotencyStore;
import com.fourati.platform.logging.RequestCorrelationFilter;
import com.fourati.platform.properties.CommonProperties;
import com.fourati.platform.ratelimit.InMemoryRateLimitStore;
import com.fourati.platform.ratelimit.RateLimitFilter;
import com.fourati.platform.ratelimit.RateLimitStore;
import com.fourati.platform.ratelimit.RedisRateLimitStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Platform wiring. Filters and stores are created here so a host application
 * can override any of them with its own bean.
 *
 * {@code fourati.common.store} selects where rate-limit buckets and idempotency
 * keys live: {@code memory} (default, single replica) or {@code redis}
 * (shared across replicas; requires {@code spring.data.redis.*}).
 *
 * JSON goes through the application's Jackson 3 {@link JsonMapper} when one is
 * configured, so the idempotency cache and the JSON export serialise exactly
 * like the HTTP responses; a plain mapper is the fallback for hosts without it.
 */
@AutoConfiguration
@EnableConfigurationProperties(CommonProperties.class)
@ConditionalOnWebApplication
@EnableScheduling
class AdminApiPlatformAutoConfiguration {

    /** The single RFC 9457 problem-details factory shared by the handler, the filters and the security entry points. */
    @Bean
    @ConditionalOnMissingBean
    Problems problems(CommonProperties props) {
        return new Problems(props.error().baseUrl());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "fourati.common.correlation.enabled", matchIfMissing = true)
    RequestCorrelationFilter requestCorrelationFilter() {
        return new RequestCorrelationFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "fourati.common.rate-limit.enabled", matchIfMissing = true)
    RateLimitFilter rateLimitFilter(CommonProperties props, RateLimitStore store, Problems problems) {
        return new RateLimitFilter(props, store, problems);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "fourati.common.idempotency.enabled", matchIfMissing = true)
    IdempotencyFilter idempotencyFilter(IdempotencyStore store, Problems problems) {
        return new IdempotencyFilter(store, problems);
    }

    @Bean
    @ConditionalOnMissingBean
    GlobalExceptionHandler globalExceptionHandler(Problems problems) {
        return new GlobalExceptionHandler(problems);
    }

    @Bean
    @ConditionalOnMissingBean
    AuditAspect auditAspect(ApplicationEventPublisher publisher) {
        return new AuditAspect(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    DomainEventPublisher domainEventPublisher(ApplicationEventPublisher publisher) {
        return new DomainEventPublisher(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    ExportService exportService(ObjectProvider<ObjectMapper> mapperProvider) {
        return new ExportService(mapperProvider.getIfAvailable(AdminApiPlatformAutoConfiguration::plainMapper));
    }

    private static ObjectMapper plainMapper() {
        return JsonMapper.builder().build();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "fourati.common.store", havingValue = "redis")
    static class RedisStores {

        @Bean
        @ConditionalOnMissingBean
        RateLimitStore rateLimitStore(StringRedisTemplate redis) {
            return new RedisRateLimitStore(redis);
        }

        @Bean
        @ConditionalOnMissingBean
        IdempotencyStore idempotencyStore(StringRedisTemplate redis, ObjectProvider<ObjectMapper> mapperProvider) {
            return new RedisIdempotencyStore(redis, mapperProvider.getIfAvailable(AdminApiPlatformAutoConfiguration::plainMapper));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "fourati.common.store", havingValue = "memory", matchIfMissing = true)
    static class InMemoryStores {

        @Bean
        @ConditionalOnMissingBean
        RateLimitStore rateLimitStore() {
            return new InMemoryRateLimitStore();
        }

        @Bean
        @ConditionalOnMissingBean
        IdempotencyStore idempotencyStore() {
            return new InMemoryIdempotencyStore();
        }
    }
}
