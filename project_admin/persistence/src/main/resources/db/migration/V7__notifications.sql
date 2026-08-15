CREATE TABLE notification_templates (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    code             VARCHAR(100) NOT NULL,
    name             VARCHAR(150) NOT NULL,
    subject_template TEXT,
    body_template    TEXT         NOT NULL,
    channel          VARCHAR(20)  NOT NULL DEFAULT 'in_app',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT notification_templates_pkey PRIMARY KEY (id),
    CONSTRAINT notification_templates_code_key UNIQUE (code),
    CONSTRAINT notification_templates_channel_check CHECK (channel IN ('in_app', 'email', 'sms', 'push', 'webhook'))
);

CREATE TABLE notifications (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    template_id UUID,
    title       VARCHAR(255) NOT NULL,
    body        TEXT         NOT NULL,
    channel     VARCHAR(20)  NOT NULL DEFAULT 'in_app',
    data        JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT notifications_pkey PRIMARY KEY (id),
    CONSTRAINT notifications_channel_check CHECK (channel IN ('in_app', 'email', 'sms', 'push', 'webhook')),
    CONSTRAINT notifications_template_id_fkey FOREIGN KEY (template_id)
        REFERENCES notification_templates (id) ON DELETE SET NULL
);

CREATE INDEX idx_notifications_channel ON notifications USING btree (channel);

CREATE TABLE user_notifications (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    notification_id UUID        NOT NULL,
    user_id         UUID        NOT NULL,
    is_read         BOOLEAN     NOT NULL DEFAULT false,
    read_at         TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT user_notifications_pkey PRIMARY KEY (id),
    CONSTRAINT user_notifications_notification_id_fkey FOREIGN KEY (notification_id)
        REFERENCES notifications (id) ON DELETE CASCADE,
    CONSTRAINT user_notifications_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT user_notifications_notification_id_user_id_key UNIQUE (notification_id, user_id)
);

CREATE INDEX idx_user_notifications_user ON user_notifications USING btree (user_id, is_read);

CREATE TRIGGER trg_set_updated_at_notification_templates
    BEFORE UPDATE ON notification_templates
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_set_updated_at_notifications
    BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_set_updated_at_user_notifications
    BEFORE UPDATE ON user_notifications
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
