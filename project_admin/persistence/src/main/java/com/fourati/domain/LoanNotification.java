package com.fourati.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Links a platform {@link Notification} to the {@link LoanApplication}
 * whose lifecycle event triggered it. Content/channel/delivery still lives
 * on {@code notifications}/{@code user_notifications} — this entity only
 * records which loan application a notification was about and why it was
 * sent. Table has no {@code updated_at} column, so this does not extend
 * {@link BaseEntity}, matching the append-only status-history entities.
 */
@Entity
@Table(name = "loan_notifications")
@Getter
@Setter
@NoArgsConstructor
public class LoanNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
