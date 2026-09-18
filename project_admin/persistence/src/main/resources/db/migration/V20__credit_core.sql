-- ============================================================================
-- V20__credit_core.sql
-- Credit domain core: credit_checks, credit_reports, credit_scores,
-- credit_assessments, credit_check_status_history.
--
-- References loan_applications (V17) and customers (V15). Credit bureau
-- integration is abstracted behind a provider interface in the business
-- layer (mock implementation only) — this migration stores results, not
-- bureau connectivity.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- credit_checks
-- ----------------------------------------------------------------------------
CREATE TABLE credit_checks (
    id                     UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id    UUID         NOT NULL,
    customer_id            UUID         NOT NULL,
    status                 VARCHAR(20)  NOT NULL DEFAULT 'pending',
    requested_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at           TIMESTAMPTZ,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT credit_checks_pkey PRIMARY KEY (id),
    CONSTRAINT credit_checks_status_check CHECK (
        status IN ('pending', 'in_progress', 'completed', 'failed')
    ),
    CONSTRAINT credit_checks_loan_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT credit_checks_customer_id_fkey FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE RESTRICT,
    CONSTRAINT credit_checks_application_key UNIQUE (loan_application_id)
);

CREATE INDEX idx_credit_checks_customer ON credit_checks (customer_id);
CREATE INDEX idx_credit_checks_status ON credit_checks (status);

COMMENT ON TABLE credit_checks IS 'A credit bureau check performed for a loan application (one per application)';

-- ----------------------------------------------------------------------------
-- credit_reports
-- ----------------------------------------------------------------------------
CREATE TABLE credit_reports (
    id                  UUID          NOT NULL DEFAULT gen_random_uuid(),
    credit_check_id     UUID          NOT NULL,
    bureau_name         VARCHAR(50)   NOT NULL,
    report_reference    VARCHAR(100)  NOT NULL,
    raw_score           INTEGER,
    retrieved_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT credit_reports_pkey PRIMARY KEY (id),
    CONSTRAINT credit_reports_credit_check_id_fkey FOREIGN KEY (credit_check_id)
        REFERENCES credit_checks (id) ON DELETE CASCADE
);

CREATE INDEX idx_credit_reports_check ON credit_reports (credit_check_id);
CREATE UNIQUE INDEX uq_credit_reports_reference ON credit_reports (bureau_name, report_reference);

-- ----------------------------------------------------------------------------
-- credit_scores
-- ----------------------------------------------------------------------------
CREATE TABLE credit_scores (
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    credit_check_id   UUID         NOT NULL,
    score             INTEGER      NOT NULL,
    score_model       VARCHAR(50)  NOT NULL,
    scored_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT credit_scores_pkey PRIMARY KEY (id),
    CONSTRAINT credit_scores_score_check CHECK (score BETWEEN 300 AND 850),
    CONSTRAINT credit_scores_credit_check_id_fkey FOREIGN KEY (credit_check_id)
        REFERENCES credit_checks (id) ON DELETE CASCADE
);

CREATE INDEX idx_credit_scores_check ON credit_scores (credit_check_id);

-- ----------------------------------------------------------------------------
-- credit_assessments
-- ----------------------------------------------------------------------------
CREATE TABLE credit_assessments (
    id                          UUID          NOT NULL DEFAULT gen_random_uuid(),
    credit_check_id             UUID          NOT NULL,
    debt_to_income_ratio        NUMERIC(5,4),
    decision                    VARCHAR(20)   NOT NULL,
    assessed_at                 TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_at                  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT credit_assessments_pkey PRIMARY KEY (id),
    CONSTRAINT credit_assessments_decision_check CHECK (decision IN ('approve', 'refer', 'reject')),
    CONSTRAINT credit_assessments_credit_check_id_fkey FOREIGN KEY (credit_check_id)
        REFERENCES credit_checks (id) ON DELETE CASCADE,
    CONSTRAINT credit_assessments_check_key UNIQUE (credit_check_id)
);

CREATE INDEX idx_credit_assessments_check ON credit_assessments (credit_check_id);

COMMENT ON TABLE credit_assessments IS 'The credit decision derived from a credit check — one per check';

-- ----------------------------------------------------------------------------
-- credit_check_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE credit_check_status_history (
    id                 UUID         NOT NULL DEFAULT gen_random_uuid(),
    credit_check_id    UUID         NOT NULL,
    from_status        VARCHAR(20),
    to_status          VARCHAR(20)  NOT NULL,
    reason             VARCHAR(500),
    changed_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT credit_check_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT credit_check_status_history_credit_check_id_fkey FOREIGN KEY (credit_check_id)
        REFERENCES credit_checks (id) ON DELETE CASCADE
);

CREATE INDEX idx_credit_check_status_history_check ON credit_check_status_history (credit_check_id);

COMMENT ON TABLE credit_check_status_history IS 'Append-only audit trail of credit check status transitions';
