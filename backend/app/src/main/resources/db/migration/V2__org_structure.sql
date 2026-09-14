-- ============================================================================
-- V2__org_structure.sql
-- Organization structure: organizations, departments, teams, organization_members
-- Realigned to match the authoritative pg_dump schema exactly.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- organizations
-- ----------------------------------------------------------------------------
CREATE TABLE organizations (
    id                     UUID         NOT NULL DEFAULT gen_random_uuid(),
    parent_organization_id UUID,
    name                   VARCHAR(200) NOT NULL,
    code                   VARCHAR(50)  NOT NULL,
    status                 VARCHAR(20)  NOT NULL DEFAULT 'active',
    metadata               JSONB        NOT NULL DEFAULT '{}',
    deleted_at             TIMESTAMPTZ,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT organizations_pkey PRIMARY KEY (id),
    CONSTRAINT organizations_status_check CHECK (status IN ('active', 'inactive', 'archived')),
    CONSTRAINT organizations_parent_organization_id_fkey FOREIGN KEY (parent_organization_id)
        REFERENCES organizations (id) ON DELETE SET NULL
);

CREATE INDEX idx_organizations_parent ON organizations (parent_organization_id);
CREATE UNIQUE INDEX uq_organizations_code ON organizations (code) WHERE (deleted_at IS NULL);

-- ----------------------------------------------------------------------------
-- departments
-- ----------------------------------------------------------------------------
CREATE TABLE departments (
    id                    UUID         NOT NULL DEFAULT gen_random_uuid(),
    organization_id       UUID         NOT NULL,
    parent_department_id  UUID,
    name                  VARCHAR(200) NOT NULL,
    code                  VARCHAR(50),
    deleted_at            TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT departments_pkey PRIMARY KEY (id),
    CONSTRAINT departments_organization_id_fkey FOREIGN KEY (organization_id)
        REFERENCES organizations (id) ON DELETE CASCADE,
    CONSTRAINT departments_parent_department_id_fkey FOREIGN KEY (parent_department_id)
        REFERENCES departments (id) ON DELETE SET NULL
);

CREATE INDEX idx_departments_org    ON departments (organization_id);
CREATE INDEX idx_departments_parent ON departments (parent_department_id);
CREATE UNIQUE INDEX uq_departments_org_code ON departments (organization_id, code)
    WHERE (deleted_at IS NULL AND code IS NOT NULL);

-- ----------------------------------------------------------------------------
-- teams
-- ----------------------------------------------------------------------------
CREATE TABLE teams (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID         NOT NULL,
    department_id   UUID,
    name            VARCHAR(200) NOT NULL,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT teams_pkey PRIMARY KEY (id),
    CONSTRAINT teams_organization_id_fkey FOREIGN KEY (organization_id)
        REFERENCES organizations (id) ON DELETE CASCADE,
    CONSTRAINT teams_department_id_fkey FOREIGN KEY (department_id)
        REFERENCES departments (id) ON DELETE SET NULL
);

CREATE INDEX idx_teams_org        ON teams (organization_id);
CREATE INDEX idx_teams_department ON teams (department_id);

-- ----------------------------------------------------------------------------
-- organization_members
-- ----------------------------------------------------------------------------
CREATE TABLE organization_members (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    organization_id UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    department_id   UUID,
    team_id         UUID,
    title           VARCHAR(150),
    is_primary      BOOLEAN      NOT NULL DEFAULT FALSE,
    joined_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT organization_members_pkey PRIMARY KEY (id),
    CONSTRAINT organization_members_organization_id_user_id_key UNIQUE (organization_id, user_id),
    CONSTRAINT organization_members_organization_id_fkey FOREIGN KEY (organization_id)
        REFERENCES organizations (id) ON DELETE CASCADE,
    CONSTRAINT organization_members_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT organization_members_department_id_fkey FOREIGN KEY (department_id)
        REFERENCES departments (id) ON DELETE SET NULL,
    CONSTRAINT organization_members_team_id_fkey FOREIGN KEY (team_id)
        REFERENCES teams (id) ON DELETE SET NULL
);

CREATE INDEX idx_org_members_org  ON organization_members (organization_id);
CREATE INDEX idx_org_members_user ON organization_members (user_id);
