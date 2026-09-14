package com.fourati.audit.internal.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * DDL-level operations on the {@code audit_logs} partition set. Kept apart from
 * the JPA repository because partition names are identifiers, not bind
 * parameters; the name is validated against the {@code audit_logs_YYYY_MM}
 * pattern before it is interpolated.
 */
@Repository
@RequiredArgsConstructor
public class AuditLogPartitionRepository {

    private static final String NAME_PATTERN = "audit_logs_\\d{4}_\\d{2}";

    private final JdbcTemplate jdbc;

    /** Creates the monthly partition if it does not exist (V13 {@code create_monthly_partition}). */
    public void createMonthlyPartition(LocalDate monthStart) {
        jdbc.update("SELECT public.create_monthly_partition('audit_logs', ?)", java.sql.Date.valueOf(monthStart.withDayOfMonth(1)));
    }

    /** Names of every partition currently attached to {@code audit_logs}. */
    public List<String> listPartitions() {
        return jdbc.queryForList("""
                SELECT c.relname
                FROM pg_inherits i
                JOIN pg_class c ON c.oid = i.inhrelid
                JOIN pg_class p ON p.oid = i.inhparent
                WHERE p.relname = 'audit_logs'
                ORDER BY c.relname
                """, String.class);
    }

    /** Detaches and drops one monthly partition; refuses anything not matching the naming pattern. */
    public void dropPartition(String partitionName) {
        if (!partitionName.matches(NAME_PATTERN)) {
            throw new IllegalArgumentException("Refusing to drop non-monthly partition: " + partitionName);
        }
        jdbc.execute("ALTER TABLE audit_logs DETACH PARTITION " + partitionName);
        jdbc.execute("DROP TABLE " + partitionName);
    }
}
