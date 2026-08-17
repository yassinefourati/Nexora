-- ============================================================================
-- V19__kyc_aml_core.sql
-- KYC/AML domain core: kyc_cases, kyc_checks, aml_screenings, aml_alerts,
-- kyc_status_history.
--
-- References customers (V15__customer_domain_core.sql) only — independent
-- of the loan product/application/document chain (V16-V18).
-- ============================================================================

-- ----------------------------------------------------------------------------
-- kyc_cases
-- ----------------------------------------------------------------------------
CREATE TABLE kyc_cases (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    customer_id    UUID         NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'pending',
    initiated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT kyc_cases_pkey PRIMARY KEY (id),
    CONSTRAINT kyc_cases_status_check CHECK (
        status IN ('pending', 'in_progress', 'passed', 'failed', 'manual_review', 'expired')
    ),
    CONSTRAINT kyc_cases_customer_id_fkey FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE RESTRICT
);

CREATE INDEX idx_kyc_cases_customer ON kyc_cases (customer_id);
CREATE INDEX idx_kyc_cases_status ON kyc_cases (status);

COMMENT ON TABLE kyc_cases IS 'A customer identity-verification case (Know Your Customer)';

-- ----------------------------------------------------------------------------
-- kyc_checks
-- ----------------------------------------------------------------------------
CREATE TABLE kyc_checks (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    kyc_case_id   UUID         NOT NULL,
    check_type    VARCHAR(30)  NOT NULL,
    result        VARCHAR(20)  NOT NULL,
    checked_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT kyc_checks_pkey PRIMARY KEY (id),
    CONSTRAINT kyc_checks_check_type_check CHECK (
        check_type IN ('identity_document', 'liveness', 'address_verification', 'sanctions_screening')
    ),
    CONSTRAINT kyc_checks_result_check CHECK (result IN ('passed', 'failed', 'inconclusive')),
    CONSTRAINT kyc_checks_kyc_case_id_fkey FOREIGN KEY (kyc_case_id)
        REFERENCES kyc_cases (id) ON DELETE CASCADE
);

CREATE INDEX idx_kyc_checks_case ON kyc_checks (kyc_case_id);

-- ----------------------------------------------------------------------------
-- aml_screenings
-- ----------------------------------------------------------------------------
CREATE TABLE aml_screenings (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    kyc_case_id      UUID         NOT NULL,
    screening_type   VARCHAR(20)  NOT NULL,
    result           VARCHAR(20)  NOT NULL,
    screened_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT aml_screenings_pkey PRIMARY KEY (id),
    CONSTRAINT aml_screenings_screening_type_check CHECK (screening_type IN ('sanctions', 'pep', 'watchlist')),
    CONSTRAINT aml_screenings_result_check CHECK (result IN ('clear', 'match', 'potential_match')),
    CONSTRAINT aml_screenings_kyc_case_id_fkey FOREIGN KEY (kyc_case_id)
        REFERENCES kyc_cases (id) ON DELETE CASCADE
);

CREATE INDEX idx_aml_screenings_case ON aml_screenings (kyc_case_id);

COMMENT ON TABLE aml_screenings IS 'Anti-Money-Laundering screening results (sanctions/PEP/watchlist) for a KYC case';

-- ----------------------------------------------------------------------------
-- aml_alerts
-- ----------------------------------------------------------------------------
CREATE TABLE aml_alerts (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    aml_screening_id    UUID         NOT NULL,
    alert_type          VARCHAR(30)  NOT NULL,
    severity            VARCHAR(20)  NOT NULL DEFAULT 'medium',
    status              VARCHAR(20)  NOT NULL DEFAULT 'open',
    resolved_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT aml_alerts_pkey PRIMARY KEY (id),
    CONSTRAINT aml_alerts_alert_type_check CHECK (alert_type IN ('sanctions_hit', 'pep_hit', 'watchlist_hit', 'name_similarity')),
    CONSTRAINT aml_alerts_severity_check CHECK (severity IN ('low', 'medium', 'high', 'critical')),
    CONSTRAINT aml_alerts_status_check CHECK (status IN ('open', 'investigating', 'resolved', 'dismissed')),
    CONSTRAINT aml_alerts_aml_screening_id_fkey FOREIGN KEY (aml_screening_id)
        REFERENCES aml_screenings (id) ON DELETE CASCADE
);

CREATE INDEX idx_aml_alerts_screening ON aml_alerts (aml_screening_id);
CREATE INDEX idx_aml_alerts_status ON aml_alerts (status);

-- ----------------------------------------------------------------------------
-- kyc_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE kyc_status_history (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    kyc_case_id    UUID         NOT NULL,
    from_status    VARCHAR(20),
    to_status      VARCHAR(20)  NOT NULL,
    reason         VARCHAR(500),
    changed_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT kyc_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT kyc_status_history_kyc_case_id_fkey FOREIGN KEY (kyc_case_id)
        REFERENCES kyc_cases (id) ON DELETE CASCADE
);

CREATE INDEX idx_kyc_status_history_case ON kyc_status_history (kyc_case_id);

COMMENT ON TABLE kyc_status_history IS 'Append-only audit trail of KYC case status transitions';
