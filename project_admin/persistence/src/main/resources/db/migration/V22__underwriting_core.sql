-- ============================================================================
-- V22__underwriting_core.sql
-- Underwriting domain core: underwriting_cases, underwriting_conditions,
-- underwriting_notes, underwriting_status_history.
--
-- An underwriting case is opened against a loan application once its
-- credit, risk and fraud checks are available. It deliberately does NOT
-- foreign-key directly into credit_checks/risk_assessments/fraud_checks —
-- those modules are read independently (scoped by loan_application_id)
-- rather than coupled to from here, per the module-boundary principle.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- underwriting_cases
-- ----------------------------------------------------------------------------
CREATE TABLE underwriting_cases (
    id                    UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id   UUID         NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'pending',
    decision              VARCHAR(30),
    decision_reason       VARCHAR(1000),
    approved_amount       NUMERIC(15,2),
    approved_term_months  INTEGER,
    assigned_to           VARCHAR(150),
    decided_at            TIMESTAMPTZ,
    deleted_at            TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT underwriting_cases_pkey PRIMARY KEY (id),
    CONSTRAINT underwriting_cases_status_check CHECK (
        status IN ('pending', 'in_review', 'completed')
    ),
    CONSTRAINT underwriting_cases_decision_check CHECK (
        decision IS NULL OR decision IN (
            'approve', 'approve_with_conditions', 'refer', 'reject', 'request_information'
        )
    ),
    CONSTRAINT underwriting_cases_approved_amount_check CHECK (approved_amount IS NULL OR approved_amount > 0),
    CONSTRAINT underwriting_cases_approved_term_check CHECK (approved_term_months IS NULL OR approved_term_months > 0),
    CONSTRAINT underwriting_cases_loan_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE
);

CREATE INDEX idx_underwriting_cases_loan_application ON underwriting_cases (loan_application_id);
CREATE INDEX idx_underwriting_cases_status ON underwriting_cases (status);
CREATE INDEX idx_underwriting_cases_deleted_at ON underwriting_cases (deleted_at);
CREATE UNIQUE INDEX uq_underwriting_cases_loan_application ON underwriting_cases (loan_application_id) WHERE (deleted_at IS NULL);

COMMENT ON TABLE underwriting_cases IS 'One underwriting case per loan application, holding the final lending decision';

-- ----------------------------------------------------------------------------
-- underwriting_conditions
-- ----------------------------------------------------------------------------
CREATE TABLE underwriting_conditions (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    underwriting_case_id    UUID         NOT NULL,
    description             VARCHAR(500) NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'pending',
    satisfied_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT underwriting_conditions_pkey PRIMARY KEY (id),
    CONSTRAINT underwriting_conditions_status_check CHECK (
        status IN ('pending', 'satisfied', 'waived')
    ),
    CONSTRAINT underwriting_conditions_case_id_fkey FOREIGN KEY (underwriting_case_id)
        REFERENCES underwriting_cases (id) ON DELETE CASCADE
);

CREATE INDEX idx_underwriting_conditions_case ON underwriting_conditions (underwriting_case_id);

-- ----------------------------------------------------------------------------
-- underwriting_notes
-- ----------------------------------------------------------------------------
CREATE TABLE underwriting_notes (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    underwriting_case_id    UUID         NOT NULL,
    author                  VARCHAR(150) NOT NULL,
    note                    VARCHAR(2000) NOT NULL,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT underwriting_notes_pkey PRIMARY KEY (id),
    CONSTRAINT underwriting_notes_case_id_fkey FOREIGN KEY (underwriting_case_id)
        REFERENCES underwriting_cases (id) ON DELETE CASCADE
);

CREATE INDEX idx_underwriting_notes_case ON underwriting_notes (underwriting_case_id);

-- ----------------------------------------------------------------------------
-- underwriting_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE underwriting_status_history (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    underwriting_case_id    UUID         NOT NULL,
    from_status             VARCHAR(20),
    to_status               VARCHAR(20)  NOT NULL,
    reason                  VARCHAR(500),
    changed_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT underwriting_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT underwriting_status_history_case_id_fkey FOREIGN KEY (underwriting_case_id)
        REFERENCES underwriting_cases (id) ON DELETE CASCADE
);

CREATE INDEX idx_underwriting_status_history_case ON underwriting_status_history (underwriting_case_id);
