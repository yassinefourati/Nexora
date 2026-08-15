-- V8__content_extensions.sql
-- Content extension tables: tags, entity_tags, comments, attachments, metadata_kv.
-- tags must exist before entity_tags (FK). comments/attachments/metadata_kv reference
-- polymorphic (entity_type, entity_id) pairs with no DB-level FK, per real schema.

CREATE TABLE tags (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    color       VARCHAR(20),
    deleted_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_tags PRIMARY KEY (id)
);

-- Partial unique index: name must be unique among non-deleted tags.
CREATE UNIQUE INDEX uq_tags_name ON tags (name) WHERE (deleted_at IS NULL);

CREATE TABLE entity_tags (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    tag_id      UUID         NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    entity_type VARCHAR(100) NOT NULL,
    entity_id   UUID         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_entity_tags PRIMARY KEY (id),
    CONSTRAINT entity_tags_tag_id_entity_type_entity_id_key UNIQUE (tag_id, entity_type, entity_id)
);

CREATE INDEX idx_entity_tags_entity ON entity_tags (entity_type, entity_id);

CREATE TABLE comments (
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    entity_type       VARCHAR(100) NOT NULL,
    entity_id         UUID         NOT NULL,
    user_id           UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_comment_id UUID         REFERENCES comments(id) ON DELETE CASCADE,
    body              TEXT         NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_comments PRIMARY KEY (id)
);

CREATE INDEX idx_comments_entity ON comments (entity_type, entity_id);

CREATE TABLE attachments (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id   UUID         NOT NULL,
    uploaded_by UUID         REFERENCES users(id) ON DELETE SET NULL,
    file_name   VARCHAR(255) NOT NULL,
    file_url    TEXT         NOT NULL,
    mime_type   VARCHAR(150),
    size_bytes  BIGINT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_attachments PRIMARY KEY (id)
);

CREATE INDEX idx_attachments_entity ON attachments (entity_type, entity_id);

CREATE TABLE metadata_kv (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id   UUID         NOT NULL,
    key         VARCHAR(150) NOT NULL,
    value       JSONB        NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_metadata_kv PRIMARY KEY (id),
    CONSTRAINT metadata_kv_entity_type_entity_id_key_key UNIQUE (entity_type, entity_id, key)
);

CREATE INDEX idx_metadata_kv_entity ON metadata_kv (entity_type, entity_id);
CREATE INDEX idx_metadata_kv_value_gin ON metadata_kv USING gin (value);
