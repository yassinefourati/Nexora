package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A discrete system/application event captured for observability purposes
 * (e.g. lifecycle events, background job outcomes). Written internally by
 * the application; exposed via a read-only API.
 */
@Entity
@Table(name = "system_events")
@Getter
@Setter
@NoArgsConstructor
public class SystemEvent extends ObservabilityBaseEntity {

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity = "info";

    @Column(name = "source", length = 100)
    private String source;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload = "{}";
}
