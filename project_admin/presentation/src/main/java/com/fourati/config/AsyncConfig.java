package com.fourati.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async + Scheduling configuration.
 *
 * Why configure the executor explicitly?
 *   Spring's default @Async executor is SimpleAsyncTaskExecutor — it creates a NEW
 *   thread for every task. Under load this causes thread explosion and OOM.
 *   This replaces it with a bounded ThreadPoolTaskExecutor.
 *
 * Tuning guidance (app.async.*):
 *   core-pool-size  = threads always alive (set to # of CPU cores for CPU-bound, higher for I/O)
 *   max-pool-size   = hard ceiling (set 2-4× core for I/O-bound tasks)
 *   queue-capacity  = backlog before spinning up to max (0 = immediate scale-up)
 *
 * Rejection policy: CallerRunsPolicy — when the queue is full, the calling thread
 * executes the task itself. This provides back-pressure instead of dropping work.
 */
@Configuration
@EnableAsync
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    private final AppProperties props;

    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        AppProperties.Async cfg = props.async();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cfg.corePoolSize());
        executor.setMaxPoolSize(cfg.maxPoolSize());
        executor.setQueueCapacity(cfg.queueCapacity());
        executor.setThreadNamePrefix(cfg.threadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("Async executor configured: core={} max={} queue={}", cfg.corePoolSize(), cfg.maxPoolSize(), cfg.queueCapacity());
        return executor;
    }

    /** Logs any @Async method that throws an uncaught exception. */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error("Uncaught exception in @Async method {}: {}", method.getName(), ex.getMessage(), ex);
    }

}
