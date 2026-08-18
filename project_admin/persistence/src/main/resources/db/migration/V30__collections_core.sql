-- ============================================================================
-- V30__collections_core.sql
-- Collections domain core: collection_cases, collection_notes,
-- collection_case_status_history.
--
-- A collection case is opened against a loan_account once one of its
-- installments falls overdue, tracking escalation through to resolution or
-- write-off. References loan_accounts and loan_installments — the
-- overdue installment that triggered the case — the only other module it
-- is coupled to.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- collection_cases
-- ----------------------------------------------------------------------------
CREATE TABLE collection_cases (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_account_id         UUID         NOT NULL,
    loan_installment_id     UUID         NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'open',
    stage                   VARCHAR(20)  NOT NULL DEFAULT 'reminder',
    assigned_to             VARCHAR(150),
    overdue_amount          NUMERIC(15,2) NOT NULL,
    resolution_notes        VARCHAR(1000),
    opened_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    resolved_at             TIMESTAMPTZ,
    deleted_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT collection_cases_pkey PRIMARY KEY (id),
    CONSTRAINT collection_cases_status_check CHECK (
        status IN ('open', 'in_progress', 'resolved', 'written_off')
    ),
    CONSTRAINT collection_cases_stage_check CHECK (
        stage IN ('reminder', 'notice', 'final_notice', 'agency')
    ),
    CONSTRAINT collection_cases_overdue_amount_check CHECK (overdue_amount > 0),
    CONSTRAINT collection_cases_loan_account_id_fkey FOREIGN KEY (loan_account_id)
        REFERENCES loan_accounts (id) ON DELETE CASCADE,
    CONSTRAINT collection_cases_loan_installment_id_fkey FOREIGN KEY (loan_installment_id)
        REFERENCES loan_installments (id) ON DELETE CASCADE
);

CREATE INDEX idx_collection_cases_loan_account ON collection_cases (loan_account_id);
CREATE INDEX idx_collection_cases_loan_installment ON collection_cases (loan_installment_id);
CREATE INDEX idx_collection_cases_status ON collection_cases (status);
CREATE INDEX idx_collection_cases_deleted_at ON collection_cases (deleted_at);
CREATE UNIQUE INDEX uq_collection_cases_loan_installment ON collection_cases (loan_installment_id) WHERE (deleted_at IS NULL);

COMMENT ON TABLE collection_cases IS 'A case tracking collection efforts on one overdue loan_installment through to resolution or write-off';

-- ----------------------------------------------------------------------------
-- collection_notes
-- ----------------------------------------------------------------------------
CREATE TABLE collection_notes (
    id                      UUID          NOT NULL DEFAULT gen_random_uuid(),
    collection_case_id      UUID          NOT NULL,
    author                  VARCHAR(150)  NOT NULL,
    note                    VARCHAR(2000) NOT NULL,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT collection_notes_pkey PRIMARY KEY (id),
    CONSTRAINT collection_notes_case_id_fkey FOREIGN KEY (collection_case_id)
        REFERENCES collection_cases (id) ON DELETE CASCADE
);

CREATE INDEX idx_collection_notes_case ON collection_notes (collection_case_id);

-- ----------------------------------------------------------------------------
-- collection_case_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE collection_case_status_history (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    collection_case_id      UUID         NOT NULL,
    from_status             VARCHAR(20),
    to_status               VARCHAR(20)  NOT NULL,
    reason                  VARCHAR(500),
    changed_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT collection_case_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT collection_case_status_history_case_id_fkey FOREIGN KEY (collection_case_id)
        REFERENCES collection_cases (id) ON DELETE CASCADE
);

CREATE INDEX idx_collection_case_status_history_case ON collection_case_status_history (collection_case_id);
