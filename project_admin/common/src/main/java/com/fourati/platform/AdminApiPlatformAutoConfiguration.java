package com.fourati.platform;

import com.fourati.platform.audit.AuditAspect;
import com.fourati.platform.error.GlobalExceptionHandler;
import com.fourati.platform.events.DomainEventPublisher;
import com.fourati.platform.export.ExportService;
import com.fourati.platform.idempotency.IdempotencyFilter;
import com.fourati.platform.logging.RequestCorrelationFilter;
import com.fourati.platform.properties.CommonProperties;
import com.fourati.platform.ratelimit.RateLimitFilter;
import com.fourati.platform.security.SqlInjectionFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@EnableConfigurationProperties(CommonProperties.class)
@ConditionalOnWebApplication
@EnableScheduling
class AdminApiPlatformAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "fourati.common.correlation.enabled", matchIfMissing = true)
    RequestCorrelationFilter requestCorrelationFilter() {
        return new RequestCorrelationFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "fourati.common.rate-limit.enabled", matchIfMissing = true)
    RateLimitFilter rateLimitFilter(CommonProperties props) {
        return new RateLimitFilter(props);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "fourati.common.sql-injection.enabled", matchIfMissing = true)
    SqlInjectionFilter sqlInjectionFilter() {
        return new SqlInjectionFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "fourati.common.idempotency.enabled", matchIfMissing = true)
    IdempotencyFilter idempotencyFilter() {
        return new IdempotencyFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    GlobalExceptionHandler globalExceptionHandler(CommonProperties props) {
        return new GlobalExceptionHandler(props);
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
        return new ExportService(mapperProvider.getIfAvailable(
            () -> new ObjectMapper().findAndRegisterModules()));
    }
}
