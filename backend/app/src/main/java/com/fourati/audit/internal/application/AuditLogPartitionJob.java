package com.fourati.audit.internal.application;

import com.fourati.audit.internal.persistence.AuditLogPartitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Data lifecycle for {@code audit_logs}, which is range-partitioned by month (V5).
 *
 *  - Ahead: makes sure partitions exist for this month and the next
 *    {@code app.audit.partitions-ahead} months, using the V13
 *    {@code create_monthly_partition()} function. Without this, rows would
 *    silently fall into {@code audit_logs_default} once the seeded partitions
 *    are exhausted, and the default partition can never be dropped cheaply.
 *  - Retention: detaches and drops partitions older than
 *    {@code app.audit.retention-months}. Dropping a partition is O(1); a DELETE
 *    over the same rows would bloat the table and hold locks for minutes.
 *
 * Runs nightly and once shortly after start-up so a fresh environment is never
 * without a current partition. Both entry points share one ShedLock name, so a
 * fleet of replicas starting together runs the start-up pass exactly once
 * (ADR 0008).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogPartitionJob {

    static final String LOCK = "auditLogPartitionMaintenance";

    private final AuditLogPartitionRepository partitions;

    @Value("${app.audit.partitions-ahead:2}")
    private int partitionsAhead;

    @Value("${app.audit.retention-months:24}")
    private int retentionMonths;

    @Scheduled(cron = "${app.audit.partition-cron:0 15 2 * * *}")
    @SchedulerLock(name = LOCK, lockAtMostFor = "10m", lockAtLeastFor = "1m")
    @Transactional
    public void maintain() {
        ensureUpcomingPartitions();
        dropExpiredPartitions();
    }

    @Scheduled(initialDelayString = "${app.audit.partition-initial-delay-ms:30000}", fixedDelay = Long.MAX_VALUE)
    @SchedulerLock(name = LOCK, lockAtMostFor = "10m", lockAtLeastFor = "1m")
    @Transactional
    public void maintainOnStartup() {
        ensureUpcomingPartitions();
        dropExpiredPartitions();
    }

    void ensureUpcomingPartitions() {
        LocalDate month = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1);
        for (int i = 0; i <= partitionsAhead; i++) {
            LocalDate start = month.plusMonths(i);
            try {
                partitions.createMonthlyPartition(start);
            } catch (RuntimeException ex) {
                log.warn("audit_logs partition for {} could not be created: {}", start, ex.getMessage());
            }
        }
    }

    void dropExpiredPartitions() {
        if (retentionMonths <= 0) {
            return;
        }
        LocalDate cutoff = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1).minusMonths(retentionMonths);
        List<String> names = partitions.listPartitions();
        for (String name : names) {
            // audit_logs_YYYY_MM
            String suffix = name.substring("audit_logs_".length());
            if (!suffix.matches("\\d{4}_\\d{2}")) {
                continue; // audit_logs_default and anything hand-made are left alone
            }
            LocalDate partitionMonth = LocalDate.of(Integer.parseInt(suffix.substring(0, 4)), Integer.parseInt(suffix.substring(5)), 1);
            if (partitionMonth.isBefore(cutoff)) {
                try {
                    partitions.dropPartition(name);
                    log.info("Dropped audit_logs partition {} (older than {} months)", name, retentionMonths);
                } catch (RuntimeException ex) {
                    log.warn("audit_logs partition {} could not be dropped: {}", name, ex.getMessage());
                }
            }
        }
    }
}
