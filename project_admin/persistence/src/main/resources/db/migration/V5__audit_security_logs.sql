-- V5: audit & security logging tables
-- audit_logs (partitioned), auth_logs, login_history, sessions, api_keys,
-- user_identity_providers, rate_limits
--
-- Realigned to match the authoritative pg_dump schema exactly (see
-- db_admin-202608131126.sql). None of these 7 tables have created_by/
-- updated_by columns in the real schema.

CREATE TABLE api_keys (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL,
    name         VARCHAR(150) NOT NULL,
    key_hash     TEXT         NOT NULL,
    scopes       TEXT[]       NOT NULL DEFAULT '{}',
    last_used_at TIMESTAMPTZ,
    expires_at   TIMESTAMPTZ,
    revoked_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT api_keys_pkey PRIMARY KEY (id),
    CONSTRAINT api_keys_key_hash_key UNIQUE (key_hash),
    CONSTRAINT api_keys_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_api_keys_user ON api_keys (user_id);

-- audit_logs: real partitioned table, PARTITION BY RANGE (created_at),
-- composite PK (id, created_at), 4 partitions attached below.
CREATE TABLE audit_logs (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID,
    action      VARCHAR(50)  NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id   UUID,
    before_data JSONB,
    after_data  JSONB,
    ip_address  INET,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT audit_logs_pkey PRIMARY KEY (id, created_at),
    CONSTRAINT audit_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
)
PARTITION BY RANGE (created_at);

CREATE INDEX idx_audit_logs_entity ON ONLY audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_user   ON ONLY audit_logs (user_id);

CREATE TABLE audit_logs_2026_06 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-06-01 00:00:00-04') TO ('2026-07-01 00:00:00-04');

CREATE TABLE audit_logs_2026_07 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-07-01 00:00:00-04') TO ('2026-08-01 00:00:00-04');

CREATE TABLE audit_logs_2026_08 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-08-01 00:00:00-04') TO ('2026-09-01 00:00:00-04');

CREATE TABLE audit_logs_default PARTITION OF audit_logs DEFAULT;

CREATE TABLE auth_logs (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    UUID,
    event_type VARCHAR(30)  NOT NULL,
    ip_address INET,
    user_agent TEXT,
    metadata   JSONB        NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT auth_logs_pkey PRIMARY KEY (id),
    CONSTRAINT auth_logs_event_type_check CHECK (
        (event_type)::text = ANY (ARRAY[
            'login_success', 'login_failed', 'logout', 'password_reset',
            'password_change', 'mfa_challenge', 'account_locked', 'account_unlocked'
        ]::text[])
    ),
    CONSTRAINT auth_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_auth_logs_event_type ON auth_logs (event_type);
CREATE INDEX idx_auth_logs_user       ON auth_logs (user_id);

CREATE TABLE login_history (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    ip_address INET,
    user_agent TEXT,
    login_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    logout_at  TIMESTAMPTZ,
    success    BOOLEAN      NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT login_history_pkey PRIMARY KEY (id),
    CONSTRAINT login_history_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_login_history_user ON login_history (user_id);

CREATE TABLE rate_limits (
    id                 UUID         NOT NULL DEFAULT gen_random_uuid(),
    scope_key          VARCHAR(255) NOT NULL,
    window_started_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    request_count      INTEGER      NOT NULL DEFAULT 1,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT rate_limits_pkey PRIMARY KEY (id),
    CONSTRAINT rate_limits_scope_key_window_started_at_key UNIQUE (scope_key, window_started_at)
);

CREATE INDEX idx_rate_limits_scope ON rate_limits (scope_key, window_started_at);

CREATE TABLE sessions (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    token_hash TEXT         NOT NULL,
    ip_address INET,
    user_agent TEXT,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT sessions_pkey PRIMARY KEY (id),
    CONSTRAINT sessions_token_hash_key UNIQUE (token_hash),
    CONSTRAINT sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_sessions_expires ON sessions (expires_at);
CREATE INDEX idx_sessions_user    ON sessions (user_id);

CREATE TABLE user_identity_providers (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL,
    provider         VARCHAR(50)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    raw_profile      JSONB        NOT NULL DEFAULT '{}',
    linked_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT user_identity_providers_pkey PRIMARY KEY (id),
    CONSTRAINT user_identity_providers_provider_provider_user_id_key UNIQUE (provider, provider_user_id),
    CONSTRAINT user_identity_providers_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_identity_providers_user ON user_identity_providers (user_id);
