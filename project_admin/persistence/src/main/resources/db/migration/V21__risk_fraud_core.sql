-- ============================================================================
-- V21__risk_fraud_core.sql
-- Risk/Fraud domain core: risk_assessments, risk_assessment_factors,
-- fraud_checks, fraud_alerts, risk_assessment_status_history.
--
-- References loan_applications (V17). Fraud rules are deliberately never
-- exposed outside this module — no read endpoint returns rule definitions,
-- only outcomes.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- risk_assessments
-- ----------------------------------------------------------------------------
CREATE TABLE risk_assessments (
    id                    UUID          NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id   UUID          NOT NULL,
    status                VARCHAR(20)   NOT NULL DEFAULT 'pending',
    risk_score            INTEGER,
    risk_class            VARCHAR(20),
    assessed_at           TIMESTAMPTZ,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT risk_assessments_pkey PRIMARY KEY (id),
    CONSTRAINT risk_assessments_status_check CHECK (
        status IN ('pending', 'in_progress', 'completed', 'failed')
    ),
    CONSTRAINT risk_assessments_risk_class_check CHECK (
        risk_class IS NULL OR risk_class IN ('low', 'medium', 'high', 'very_high')
    ),
    CONSTRAINT risk_assessments_risk_score_check CHECK (risk_score IS NULL OR risk_score BETWEEN 0 AND 100),
    CONSTRAINT risk_assessments_loan_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT risk_assessments_application_key UNIQUE (loan_application_id)
);

CREATE INDEX idx_risk_assessments_status ON risk_assessments (status);

COMMENT ON TABLE risk_assessments IS 'Overall risk assessment for a loan application — one per application';

-- ----------------------------------------------------------------------------
-- risk_assessment_factors
-- ----------------------------------------------------------------------------
CREATE TABLE risk_assessment_factors (
    id                    UUID          NOT NULL DEFAULT gen_random_uuid(),
    risk_assessment_id    UUID          NOT NULL,
    factor_type           VARCHAR(30)   NOT NULL,
    factor_value          NUMERIC(10,4) NOT NULL,
    weight                NUMERIC(5,4)  NOT NULL,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT risk_assessment_factors_pkey PRIMARY KEY (id),
    CONSTRAINT risk_assessment_factors_type_check CHECK (
        factor_type IN ('credit_score', 'debt_to_income_ratio', 'income_stability',
            'employment_duration', 'loan_to_income_ratio', 'existing_debt')
    ),
    CONSTRAINT risk_assessment_factors_assessment_id_fkey FOREIGN KEY (risk_assessment_id)
        REFERENCES risk_assessments (id) ON DELETE CASCADE
);

CREATE INDEX idx_risk_assessment_factors_assessment ON risk_assessment_factors (risk_assessment_id);

-- ----------------------------------------------------------------------------
-- fraud_checks
-- ----------------------------------------------------------------------------
CREATE TABLE fraud_checks (
    id                    UUID          NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id   UUID          NOT NULL,
    status                VARCHAR(20)   NOT NULL DEFAULT 'pending',
    fraud_score           INTEGER,
    checked_at            TIMESTAMPTZ,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fraud_checks_pkey PRIMARY KEY (id),
    CONSTRAINT fraud_checks_status_check CHECK (
        status IN ('pending', 'in_progress', 'clear', 'flagged')
    ),
    CONSTRAINT fraud_checks_fraud_score_check CHECK (fraud_score IS NULL OR fraud_score BETWEEN 0 AND 100),
    CONSTRAINT fraud_checks_loan_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT fraud_checks_application_key UNIQUE (loan_application_id)
);

CREATE INDEX idx_fraud_checks_status ON fraud_checks (status);

COMMENT ON TABLE fraud_checks IS 'Fraud screening for a loan application — one per application';

-- ----------------------------------------------------------------------------
-- fraud_alerts
-- ----------------------------------------------------------------------------
CREATE TABLE fraud_alerts (
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    fraud_check_id    UUID         NOT NULL,
    indicator_type    VARCHAR(30)  NOT NULL,
    severity          VARCHAR(20)  NOT NULL DEFAULT 'medium',
    status             VARCHAR(20)  NOT NULL DEFAULT 'open',
    resolved_at       TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fraud_alerts_pkey PRIMARY KEY (id),
    CONSTRAINT fraud_alerts_indicator_type_check CHECK (
        indicator_type IN ('duplicate_application', 'identity_mismatch', 'velocity', 'device_anomaly', 'suspicious_behavior')
    ),
    CONSTRAINT fraud_alerts_severity_check CHECK (severity IN ('low', 'medium', 'high', 'critical')),
    CONSTRAINT fraud_alerts_status_check CHECK (status IN ('open', 'investigating', 'resolved', 'dismissed')),
    CONSTRAINT fraud_alerts_fraud_check_id_fkey FOREIGN KEY (fraud_check_id)
        REFERENCES fraud_checks (id) ON DELETE CASCADE
);

CREATE INDEX idx_fraud_alerts_check ON fraud_alerts (fraud_check_id);
CREATE INDEX idx_fraud_alerts_status ON fraud_alerts (status);

-- ----------------------------------------------------------------------------
-- risk_assessment_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE risk_assessment_status_history (
    id                    UUID         NOT NULL DEFAULT gen_random_uuid(),
    risk_assessment_id    UUID         NOT NULL,
    from_status           VARCHAR(20),
    to_status             VARCHAR(20)  NOT NULL,
    reason                VARCHAR(500),
    changed_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT risk_assessment_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT risk_assessment_status_history_assessment_id_fkey FOREIGN KEY (risk_assessment_id)
        REFERENCES risk_assessments (id) ON DELETE CASCADE
);

CREATE INDEX idx_risk_assessment_status_history_assessment ON risk_assessment_status_history (risk_assessment_id);

COMMENT ON TABLE risk_assessment_status_history IS 'Append-only audit trail of risk assessment status transitions';
