-- ============================================================================
-- V16__loan_product_core.sql
-- Loan product domain core: loan_products, loan_product_versions,
-- loan_product_rate_rules, loan_product_fee_rules,
-- loan_product_eligibility_rules.
--
-- A configurable catalog of loan products (personal, auto, mortgage, etc.)
-- so new products can be introduced without code changes to the
-- application/underwriting logic.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- loan_products
-- ----------------------------------------------------------------------------
CREATE TABLE loan_products (
    id                UUID          NOT NULL DEFAULT gen_random_uuid(),
    code              VARCHAR(50)   NOT NULL,
    name              VARCHAR(200)  NOT NULL,
    product_type      VARCHAR(30)   NOT NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'active',
    currency          VARCHAR(3)    NOT NULL DEFAULT 'USD',
    min_amount        NUMERIC(15,2) NOT NULL,
    max_amount        NUMERIC(15,2) NOT NULL,
    min_term_months   INTEGER       NOT NULL,
    max_term_months   INTEGER       NOT NULL,
    description       TEXT,
    deleted_at        TIMESTAMPTZ,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT loan_products_pkey PRIMARY KEY (id),
    CONSTRAINT loan_products_product_type_check CHECK (
        product_type IN ('personal', 'consumer', 'auto', 'mortgage', 'business', 'credit_line')
    ),
    CONSTRAINT loan_products_status_check CHECK (status IN ('active', 'inactive', 'retired')),
    CONSTRAINT loan_products_amount_check CHECK (min_amount >= 0 AND max_amount >= min_amount),
    CONSTRAINT loan_products_term_check CHECK (min_term_months >= 1 AND max_term_months >= min_term_months)
);

CREATE INDEX idx_loan_products_status ON loan_products (status);
CREATE INDEX idx_loan_products_deleted_at ON loan_products (deleted_at);
CREATE UNIQUE INDEX uq_loan_products_code ON loan_products (code) WHERE (deleted_at IS NULL);

COMMENT ON TABLE loan_products IS 'Configurable catalog of loan products offered by the platform';

-- ----------------------------------------------------------------------------
-- loan_product_versions
-- ----------------------------------------------------------------------------
CREATE TABLE loan_product_versions (
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_product_id   UUID         NOT NULL,
    version_number    INTEGER      NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'draft',
    effective_from    TIMESTAMPTZ,
    effective_to      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_product_versions_pkey PRIMARY KEY (id),
    CONSTRAINT loan_product_versions_status_check CHECK (status IN ('draft', 'active', 'superseded')),
    CONSTRAINT loan_product_versions_loan_product_id_fkey FOREIGN KEY (loan_product_id)
        REFERENCES loan_products (id) ON DELETE CASCADE,
    CONSTRAINT loan_product_versions_product_version_key UNIQUE (loan_product_id, version_number)
);

CREATE INDEX idx_loan_product_versions_product ON loan_product_versions (loan_product_id);

-- ----------------------------------------------------------------------------
-- loan_product_rate_rules
-- ----------------------------------------------------------------------------
CREATE TABLE loan_product_rate_rules (
    id                UUID          NOT NULL DEFAULT gen_random_uuid(),
    loan_product_id   UUID          NOT NULL,
    rate_type         VARCHAR(20)   NOT NULL,
    base_rate         NUMERIC(6,4)  NOT NULL,
    margin            NUMERIC(6,4),
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT loan_product_rate_rules_pkey PRIMARY KEY (id),
    CONSTRAINT loan_product_rate_rules_rate_type_check CHECK (rate_type IN ('fixed', 'variable')),
    CONSTRAINT loan_product_rate_rules_base_rate_check CHECK (base_rate >= 0),
    CONSTRAINT loan_product_rate_rules_loan_product_id_fkey FOREIGN KEY (loan_product_id)
        REFERENCES loan_products (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_product_rate_rules_product ON loan_product_rate_rules (loan_product_id);

-- ----------------------------------------------------------------------------
-- loan_product_fee_rules
-- ----------------------------------------------------------------------------
CREATE TABLE loan_product_fee_rules (
    id                UUID          NOT NULL DEFAULT gen_random_uuid(),
    loan_product_id   UUID          NOT NULL,
    fee_type          VARCHAR(30)   NOT NULL,
    fee_amount        NUMERIC(15,2),
    fee_percentage    NUMERIC(6,4),
    is_mandatory      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT loan_product_fee_rules_pkey PRIMARY KEY (id),
    CONSTRAINT loan_product_fee_rules_fee_type_check CHECK (
        fee_type IN ('origination', 'late_payment', 'early_repayment', 'processing')
    ),
    CONSTRAINT loan_product_fee_rules_amount_or_percentage_check CHECK (
        fee_amount IS NOT NULL OR fee_percentage IS NOT NULL
    ),
    CONSTRAINT loan_product_fee_rules_loan_product_id_fkey FOREIGN KEY (loan_product_id)
        REFERENCES loan_products (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_product_fee_rules_product ON loan_product_fee_rules (loan_product_id);

-- ----------------------------------------------------------------------------
-- loan_product_eligibility_rules
-- ----------------------------------------------------------------------------
CREATE TABLE loan_product_eligibility_rules (
    id                       UUID          NOT NULL DEFAULT gen_random_uuid(),
    loan_product_id          UUID          NOT NULL,
    min_credit_score         INTEGER,
    min_monthly_income       NUMERIC(15,2),
    max_debt_to_income_ratio NUMERIC(5,4),
    min_age                  INTEGER,
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT loan_product_eligibility_rules_pkey PRIMARY KEY (id),
    CONSTRAINT loan_product_eligibility_rules_loan_product_id_fkey FOREIGN KEY (loan_product_id)
        REFERENCES loan_products (id) ON DELETE CASCADE,
    CONSTRAINT loan_product_eligibility_rules_product_key UNIQUE (loan_product_id)
);
