-- ============================================================================
-- V18__document_core.sql
-- Document domain core: documents, document_versions,
-- application_documents, document_reviews, document_requirements.
--
-- PostgreSQL stores metadata and object-storage references only — the
-- actual file bytes live in an external object store (MinIO, per the
-- earlier architecture decision), which is not wired up in this migration.
-- References loan_applications (V17) and loan_products (V16).
-- ============================================================================

-- ----------------------------------------------------------------------------
-- documents
-- ----------------------------------------------------------------------------
CREATE TABLE documents (
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    document_type  VARCHAR(30)   NOT NULL,
    category       VARCHAR(30)   NOT NULL,
    file_name      VARCHAR(255)  NOT NULL,
    storage_key    VARCHAR(500)  NOT NULL,
    content_type   VARCHAR(100),
    size_bytes     BIGINT,
    status         VARCHAR(20)   NOT NULL DEFAULT 'uploaded',
    uploaded_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT documents_pkey PRIMARY KEY (id),
    CONSTRAINT documents_document_type_check CHECK (
        document_type IN ('identity', 'proof_of_address', 'proof_of_income', 'employment_letter',
            'bank_statement', 'tax_document', 'credit_report', 'signed_contract', 'loan_offer', 'other')
    ),
    CONSTRAINT documents_category_check CHECK (category IN ('identity', 'financial', 'legal', 'supporting')),
    CONSTRAINT documents_status_check CHECK (
        status IN ('uploaded', 'under_review', 'verified', 'rejected', 'expired', 'superseded')
    ),
    CONSTRAINT documents_size_bytes_check CHECK (size_bytes IS NULL OR size_bytes >= 0)
);

CREATE UNIQUE INDEX uq_documents_storage_key ON documents (storage_key);
CREATE INDEX idx_documents_status ON documents (status);
CREATE INDEX idx_documents_deleted_at ON documents (deleted_at);

COMMENT ON TABLE documents IS 'Metadata and object-storage reference for an uploaded document; file bytes live in external object storage';

-- ----------------------------------------------------------------------------
-- document_versions
-- ----------------------------------------------------------------------------
CREATE TABLE document_versions (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    document_id     UUID         NOT NULL,
    version_number  INTEGER      NOT NULL,
    storage_key     VARCHAR(500) NOT NULL,
    uploaded_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT document_versions_pkey PRIMARY KEY (id),
    CONSTRAINT document_versions_document_id_fkey FOREIGN KEY (document_id)
        REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT document_versions_document_version_key UNIQUE (document_id, version_number)
);

CREATE INDEX idx_document_versions_document ON document_versions (document_id);

-- ----------------------------------------------------------------------------
-- application_documents
-- ----------------------------------------------------------------------------
CREATE TABLE application_documents (
    id                    UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id   UUID         NOT NULL,
    document_id           UUID         NOT NULL,
    requirement_type       VARCHAR(30)  NOT NULL,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT application_documents_pkey PRIMARY KEY (id),
    CONSTRAINT application_documents_loan_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT application_documents_document_id_fkey FOREIGN KEY (document_id)
        REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT application_documents_unique_key UNIQUE (loan_application_id, document_id)
);

CREATE INDEX idx_application_documents_application ON application_documents (loan_application_id);
CREATE INDEX idx_application_documents_document ON application_documents (document_id);

-- ----------------------------------------------------------------------------
-- document_reviews
-- ----------------------------------------------------------------------------
CREATE TABLE document_reviews (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    document_id   UUID         NOT NULL,
    decision      VARCHAR(20)  NOT NULL,
    comments      VARCHAR(1000),
    reviewed_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT document_reviews_pkey PRIMARY KEY (id),
    CONSTRAINT document_reviews_decision_check CHECK (decision IN ('verified', 'rejected')),
    CONSTRAINT document_reviews_document_id_fkey FOREIGN KEY (document_id)
        REFERENCES documents (id) ON DELETE CASCADE
);

CREATE INDEX idx_document_reviews_document ON document_reviews (document_id);

COMMENT ON TABLE document_reviews IS 'Append-only audit trail of document review decisions';

-- ----------------------------------------------------------------------------
-- document_requirements
-- ----------------------------------------------------------------------------
CREATE TABLE document_requirements (
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_product_id   UUID         NOT NULL,
    document_type     VARCHAR(30)  NOT NULL,
    is_mandatory      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT document_requirements_pkey PRIMARY KEY (id),
    CONSTRAINT document_requirements_document_type_check CHECK (
        document_type IN ('identity', 'proof_of_address', 'proof_of_income', 'employment_letter',
            'bank_statement', 'tax_document', 'credit_report', 'signed_contract', 'loan_offer', 'other')
    ),
    CONSTRAINT document_requirements_loan_product_id_fkey FOREIGN KEY (loan_product_id)
        REFERENCES loan_products (id) ON DELETE CASCADE,
    CONSTRAINT document_requirements_unique_key UNIQUE (loan_product_id, document_type)
);

CREATE INDEX idx_document_requirements_product ON document_requirements (loan_product_id);

COMMENT ON TABLE document_requirements IS 'Configurable per-product checklist of required document types';
