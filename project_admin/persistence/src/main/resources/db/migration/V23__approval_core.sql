-- ============================================================================
-- V23__approval_core.sql
-- Approval domain core: loan_approvals, loan_approval_conditions,
-- loan_approval_status_history.
--
-- A loan approval is issued off a completed underwriting_case decision. It
-- foreign-keys to both loan_applications (the subject) and underwriting_cases
-- (the decision it formalizes) — the only other module this one is coupled
-- to, since an approval cannot exist without an underwriting decision.
-- Credit/risk/fraud data is not referenced here either, for the same
-- module-boundary reason applied in the underwriting migration.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- loan_approvals
-- ----------------------------------------------------------------------------
CREATE TABLE loan_approvals (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id     UUID         NOT NULL,
    underwriting_case_id    UUID         NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'pending',
    approved_amount         NUMERIC(15,2),
    approved_term_months    INTEGER,
    interest_rate           NUMERIC(6,3),
    approved_by             VARCHAR(150),
    rejection_reason        VARCHAR(1000),
    expires_at              TIMESTAMPTZ,
    approved_at             TIMESTAMPTZ,
    deleted_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_approvals_pkey PRIMARY KEY (id),
    CONSTRAINT loan_approvals_status_check CHECK (
        status IN ('pending', 'approved', 'rejected', 'expired')
    ),
    CONSTRAINT loan_approvals_approved_amount_check CHECK (approved_amount IS NULL OR approved_amount > 0),
    CONSTRAINT loan_approvals_approved_term_check CHECK (approved_term_months IS NULL OR approved_term_months > 0),
    CONSTRAINT loan_approvals_interest_rate_check CHECK (interest_rate IS NULL OR interest_rate >= 0),
    CONSTRAINT loan_approvals_loan_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT loan_approvals_underwriting_case_id_fkey FOREIGN KEY (underwriting_case_id)
        REFERENCES underwriting_cases (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_approvals_loan_application ON loan_approvals (loan_application_id);
CREATE INDEX idx_loan_approvals_underwriting_case ON loan_approvals (underwriting_case_id);
CREATE INDEX idx_loan_approvals_status ON loan_approvals (status);
CREATE INDEX idx_loan_approvals_deleted_at ON loan_approvals (deleted_at);
CREATE UNIQUE INDEX uq_loan_approvals_loan_application ON loan_approvals (loan_application_id) WHERE (deleted_at IS NULL);

COMMENT ON TABLE loan_approvals IS 'One approval record per loan application, formalizing an underwriting decision into approved terms';

-- ----------------------------------------------------------------------------
-- loan_approval_conditions
-- ----------------------------------------------------------------------------
CREATE TABLE loan_approval_conditions (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_approval_id    UUID         NOT NULL,
    description         VARCHAR(500) NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'pending',
    satisfied_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_approval_conditions_pkey PRIMARY KEY (id),
    CONSTRAINT loan_approval_conditions_status_check CHECK (
        status IN ('pending', 'satisfied', 'waived')
    ),
    CONSTRAINT loan_approval_conditions_approval_id_fkey FOREIGN KEY (loan_approval_id)
        REFERENCES loan_approvals (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_approval_conditions_approval ON loan_approval_conditions (loan_approval_id);

-- ----------------------------------------------------------------------------
-- loan_approval_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE loan_approval_status_history (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_approval_id    UUID         NOT NULL,
    from_status         VARCHAR(20),
    to_status           VARCHAR(20)  NOT NULL,
    reason              VARCHAR(500),
    changed_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_approval_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT loan_approval_status_history_approval_id_fkey FOREIGN KEY (loan_approval_id)
        REFERENCES loan_approvals (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_approval_status_history_approval ON loan_approval_status_history (loan_approval_id);
