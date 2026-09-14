package com.fourati.app.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Scheduler locking for multi-replica deployments (ADR 0008). Every
 * {@code @Scheduled} job also carries {@code @SchedulerLock}; the lock lives in
 * the {@code shedlock} table (Flyway V17) so exactly one replica runs a job per
 * interval, whatever the deployment. Lock timestamps use the database clock so
 * replicas with drifting clocks cannot both believe they hold the lock.
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SchedulingConfig {

    @Bean
    LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build());
    }
}
