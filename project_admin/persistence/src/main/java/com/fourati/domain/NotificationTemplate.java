package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Notification template used to render notifications sent to users
 * across a given channel (email, sms, push, in-app, webhook).
 *
 * Note: notification_templates has no deleted_at/created_by/updated_by
 * columns in the real schema, hence {@link AuditableEntity} rather than
 * {@link BaseEntity}.
 */
@Entity
@Table(name = "notification_templates")
@Getter
@Setter
@NoArgsConstructor
public class NotificationTemplate extends AuditableEntity {

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "subject_template", columnDefinition = "text")
    private String subjectTemplate;

    @Column(name = "body_template", nullable = false, columnDefinition = "text")
    private String bodyTemplate;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel = "in_app";
}
