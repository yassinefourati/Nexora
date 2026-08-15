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
 * An application error/exception record captured for observability purposes.
 * Written internally by the application; exposed via a read-only API.
 */
@Entity
@Table(name = "error_logs")
@Getter
@Setter
@NoArgsConstructor
public class ErrorLog extends ObservabilityBaseEntity {

    @Column(name = "source", length = 150)
    private String source;

    @Column(name = "error_message", nullable = false, columnDefinition = "text")
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "text")
    private String stackTrace;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context", nullable = false, columnDefinition = "jsonb")
    private String context = "{}";

    @Column(name = "severity", nullable = false, length = 20)
    private String severity = "error";
}
