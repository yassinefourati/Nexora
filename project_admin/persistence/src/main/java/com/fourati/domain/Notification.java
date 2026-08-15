package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A single notification instance, optionally rendered from a
 * {@link NotificationTemplate}. Per-recipient delivery/read state lives in
 * {@link UserNotification}, not on this entity.
 *
 * Note: notifications has no deleted_at/created_by/updated_by columns in the
 * real schema, hence {@link AuditableEntity} rather than {@link BaseEntity}.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private NotificationTemplate template;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel = "in_app";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    private String data = "{}";
}
