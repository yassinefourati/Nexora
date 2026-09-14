package com.fourati.notification.internal.domain;

import com.fourati.identity.internal.domain.User;


import java.util.UUID;

import com.fourati.shared.persistence.AuditableEntity;

import com.fourati.shared.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Join entity tracking per-recipient delivery/read state of a
 * {@link Notification} for a specific {@link User}.
 *
 * Note: user_notifications has no deleted_at/created_by/updated_by columns in
 * the real schema, hence {@link AuditableEntity} rather than {@link BaseEntity}.
 */
@Entity
@Table(name = "user_notifications")
@Getter
@Setter
@NoArgsConstructor
public class UserNotification extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    /** Recipient as a plain user id: users live in the identity module (ADR 0004). */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    /** Idempotent: marking an already-read notification read keeps the original readAt. */
    public void markRead(Instant now) {
        if (!read) {
            this.read = true;
            this.readAt = now;
        }
    }
}
