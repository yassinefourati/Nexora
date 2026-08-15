CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- ============================================================================
-- users
-- ============================================================================
CREATE TABLE users (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    username                VARCHAR(100)    NOT NULL,
    email                   CITEXT          NOT NULL,
    password_hash           TEXT            NOT NULL,
    password_changed_at     TIMESTAMPTZ,
    first_name              VARCHAR(100),
    last_name               VARCHAR(100),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'active',
    is_superuser            BOOLEAN         NOT NULL DEFAULT FALSE,
    failed_login_attempts   INTEGER         NOT NULL DEFAULT 0,
    locked_until            TIMESTAMPTZ,
    last_login_at           TIMESTAMPTZ,
    metadata                JSONB           NOT NULL DEFAULT '{}',
    deleted_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_status_check CHECK (status IN ('active', 'inactive', 'suspended', 'pending', 'deleted'))
);

CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_deleted_at ON users (deleted_at);
CREATE INDEX idx_users_metadata_gin ON users USING gin (metadata);
CREATE UNIQUE INDEX uq_users_username ON users (username) WHERE (deleted_at IS NULL);
CREATE UNIQUE INDEX uq_users_email ON users (email) WHERE (deleted_at IS NULL);

COMMENT ON TABLE users IS 'Application user accounts';

-- ============================================================================
-- roles
-- ============================================================================
CREATE TABLE roles (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    is_system       BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT roles_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_roles_name ON roles (name) WHERE (deleted_at IS NULL);

COMMENT ON TABLE roles IS 'Roles that group permissions and can be assigned to users';

-- ============================================================================
-- permissions
-- ============================================================================
CREATE TABLE permissions (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    resource        VARCHAR(100)    NOT NULL,
    action          VARCHAR(20)     NOT NULL,
    code            VARCHAR(150)    GENERATED ALWAYS AS (resource || '.' || action) STORED,
    description     TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT permissions_pkey PRIMARY KEY (id),
    CONSTRAINT permissions_resource_action_key UNIQUE (resource, action),
    CONSTRAINT permissions_action_check CHECK (action IN ('read', 'write', 'edit', 'delete', 'execute', 'approve'))
);

CREATE INDEX idx_permissions_resource_action ON permissions (resource, action);

COMMENT ON TABLE permissions IS 'Fine-grained permissions identified by resource + action (code is a generated column: resource || ''.'' || action)';

-- ============================================================================
-- role_permissions (join table: roles <-> permissions)
-- ============================================================================
CREATE TABLE role_permissions (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    role_id         UUID            NOT NULL,
    permission_id   UUID            NOT NULL,
    conditions      JSONB           NOT NULL DEFAULT '{}',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT role_permissions_pkey PRIMARY KEY (id),
    CONSTRAINT role_permissions_role_id_permission_id_key UNIQUE (role_id, permission_id),
    CONSTRAINT role_permissions_role_id_fkey FOREIGN KEY (role_id)
        REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT role_permissions_permission_id_fkey FOREIGN KEY (permission_id)
        REFERENCES permissions (id) ON DELETE CASCADE
);

CREATE INDEX idx_role_permissions_role ON role_permissions (role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions (permission_id);

COMMENT ON TABLE role_permissions IS 'Join table granting permissions to roles';

-- ============================================================================
-- user_roles (join table: users <-> roles, optionally scoped to an organization)
-- ============================================================================
CREATE TABLE user_roles (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    user_id             UUID            NOT NULL,
    role_id             UUID            NOT NULL,
    organization_id     UUID,
    expires_at          TIMESTAMPTZ,
    assigned_at         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT user_roles_pkey PRIMARY KEY (id),
    CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id)
        REFERENCES roles (id) ON DELETE CASCADE
    -- fk_user_roles_organization (organization_id -> organizations.id, ON DELETE CASCADE)
    -- is added in a later migration once the organizations table exists.
);

CREATE INDEX idx_user_roles_user ON user_roles (user_id);
CREATE INDEX idx_user_roles_role ON user_roles (role_id);
CREATE INDEX idx_user_roles_org ON user_roles (organization_id);
CREATE INDEX idx_user_roles_expires ON user_roles (expires_at) WHERE (expires_at IS NOT NULL);
CREATE UNIQUE INDEX uq_user_roles_global ON user_roles (user_id, role_id) WHERE (organization_id IS NULL);
CREATE UNIQUE INDEX uq_user_roles_org ON user_roles (user_id, role_id, organization_id) WHERE (organization_id IS NOT NULL);

COMMENT ON TABLE user_roles IS 'Join table assigning roles to users, optionally scoped to an organization (organizations FK added in a later migration)';
