package com.fourati.platform.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Template for a production-grade scheduled job.
 *
 * USAGE: Copy this class into your module, add @Component, and replace doWork() with your logic.
 * This class is intentionally NOT a @Component — it is a copy-paste template.
 *
 * Built-in features:
 *   ✔ Skip-if-already-running guard (AtomicBoolean lock) — prevents overlap if a run takes longer than the interval
 *   ✔ Structured logging with duration
 *   ✔ Catches and logs exceptions so one failure doesn't stop future runs
 *   ✔ fixedDelay (not fixedRate) — interval is measured AFTER the previous run completes
 *
 * Cron examples:
 *   "0 * * * * *"       → every minute
 *   "0 0 * * * *"       → every hour
 *   "0 0 2 * * *"       → daily at 02:00
 *   "0 0 0 * * MON"     → weekly on Monday midnight
 *   "0 0/30 9-17 * * *" → every 30 min between 09:00 and 17:00
 *
 * To disable a job without deleting it:
 *   my-module.scheduler.my-job.enabled: false
 *   Then guard: if (!props.isMyJobEnabled()) return;
 */
@Slf4j
public class ScheduledJobTemplate {

    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Runs every 60 seconds (after the previous run completes).
     * Replace with @Scheduled(cron = "0 0 2 * * *") for a nightly job.
     */
    @Scheduled(fixedDelayString = "${app.scheduler.template-job.delay-ms:60000}")
    public void run() {
        if (!running.compareAndSet(false, true)) {
            log.warn("[TemplateJob] Already running — skipping this trigger");
            return;
        }

        Instant start = Instant.now();
        int processed = 0;

        try {
            log.info("[TemplateJob] Starting");
            processed = doWork();
            long ms = Instant.now().toEpochMilli() - start.toEpochMilli();
            log.info("[TemplateJob] Completed: processed={} duration={}ms", processed, ms);

        } catch (Exception ex) {
            long ms = Instant.now().toEpochMilli() - start.toEpochMilli();
            log.error("[TemplateJob] Failed after {}ms: {}", ms, ex.getMessage(), ex);

        } finally {
            running.set(false);
        }
    }

    /**
     * Override with your actual work.
     * Returns the number of items processed (used for logging).
     */
    protected int doWork() {
        // TODO: replace with real logic
        log.debug("[TemplateJob] No-op — replace doWork() with your logic");
        return 0;
    }
}
