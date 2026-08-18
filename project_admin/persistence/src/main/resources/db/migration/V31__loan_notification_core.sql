-- ============================================================================
-- V31__loan_notification_core.sql
-- Loan notification core: loan_notifications.
--
-- Rather than a new notification-delivery stack, this links the platform's
-- existing notifications table (see V7__notifications.sql) to the loan
-- application whose lifecycle event triggered it, tagged with an
-- event_type. Actual content/channel/delivery still lives on
-- notifications/user_notifications — this table only records which loan
-- application a given notification was about and why it was sent.
-- ============================================================================

CREATE TABLE loan_notifications (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id     UUID         NOT NULL,
    notification_id         UUID         NOT NULL,
    event_type              VARCHAR(50)  NOT NULL,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_notifications_pkey PRIMARY KEY (id),
    CONSTRAINT loan_notifications_loan_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT loan_notifications_notification_id_fkey FOREIGN KEY (notification_id)
        REFERENCES notifications (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_notifications_loan_application ON loan_notifications (loan_application_id);
CREATE INDEX idx_loan_notifications_event_type ON loan_notifications (event_type);
CREATE UNIQUE INDEX uq_loan_notifications_notification ON loan_notifications (notification_id);

COMMENT ON TABLE loan_notifications IS 'Links a platform notification to the loan application whose lifecycle event triggered it';
