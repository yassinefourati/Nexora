-- V3__app_config.sql
-- Tables: app_modules, feature_flags, settings

CREATE TABLE app_modules (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    key         VARCHAR(100) NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT app_modules_pkey PRIMARY KEY (id),
    CONSTRAINT app_modules_key_key UNIQUE (key)
);

CREATE TABLE feature_flags (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    key             VARCHAR(150) NOT NULL,
    name            VARCHAR(150) NOT NULL,
    description     TEXT,
    organization_id UUID,
    is_enabled      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT feature_flags_pkey PRIMARY KEY (id),
    CONSTRAINT feature_flags_organization_id_fkey FOREIGN KEY (organization_id)
        REFERENCES organizations (id) ON DELETE CASCADE
);

CREATE INDEX idx_feature_flags_org ON feature_flags (organization_id);

-- Partial unique indexes: a global flag (no org) is unique by key alone;
-- an org-scoped flag is unique by (key, organization_id).
CREATE UNIQUE INDEX uq_feature_flags_global ON feature_flags (key) WHERE (organization_id IS NULL);
CREATE UNIQUE INDEX uq_feature_flags_org ON feature_flags (key, organization_id) WHERE (organization_id IS NOT NULL);

CREATE TABLE settings (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    scope           VARCHAR(20)  NOT NULL DEFAULT 'global',
    organization_id UUID,
    key             VARCHAR(150) NOT NULL,
    value           JSONB        NOT NULL,
    description     TEXT,
    is_editable     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT settings_pkey PRIMARY KEY (id),
    CONSTRAINT settings_scope_check CHECK (scope IN ('global', 'organization')),
    CONSTRAINT settings_check CHECK (
        ((scope = 'global') AND (organization_id IS NULL))
        OR ((scope = 'organization') AND (organization_id IS NOT NULL))
    ),
    CONSTRAINT settings_organization_id_fkey FOREIGN KEY (organization_id)
        REFERENCES organizations (id) ON DELETE CASCADE
);

CREATE INDEX idx_settings_org ON settings (organization_id);
CREATE INDEX idx_settings_value_gin ON settings USING gin (value);

-- Partial unique indexes: global settings unique by (scope, key);
-- org-scoped settings unique by (scope, organization_id, key).
CREATE UNIQUE INDEX uq_settings_global ON settings (scope, key) WHERE (organization_id IS NULL);
CREATE UNIQUE INDEX uq_settings_org ON settings (scope, organization_id, key) WHERE (organization_id IS NOT NULL);
