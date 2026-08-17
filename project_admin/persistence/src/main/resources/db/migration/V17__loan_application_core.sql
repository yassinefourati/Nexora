-- ============================================================================
-- V17__loan_application_core.sql
-- Loan application domain core: loan_applications,
-- loan_application_co_applicants, loan_application_incomes,
-- loan_application_expenses, loan_application_status_history.
--
-- Ties a customer and a loan product together into an actual application.
-- References customers (V15__customer_domain_core.sql) and loan_products
-- (V16__loan_product_core.sql).
-- ============================================================================

-- ----------------------------------------------------------------------------
-- loan_applications
-- ----------------------------------------------------------------------------
CREATE TABLE loan_applications (
    id                      UUID          NOT NULL DEFAULT gen_random_uuid(),
    customer_id             UUID          NOT NULL,
    loan_product_id         UUID          NOT NULL,
    status                  VARCHAR(20)   NOT NULL DEFAULT 'draft',
    requested_amount        NUMERIC(15,2) NOT NULL,
    requested_term_months   INTEGER       NOT NULL,
    purpose                 VARCHAR(200),
    submitted_at            TIMESTAMPTZ,
    deleted_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT loan_applications_pkey PRIMARY KEY (id),
    CONSTRAINT loan_applications_status_check CHECK (
        status IN ('draft', 'submitted', 'under_review', 'approved', 'rejected', 'withdrawn', 'cancelled')
    ),
    CONSTRAINT loan_applications_requested_amount_check CHECK (requested_amount > 0),
    CONSTRAINT loan_applications_requested_term_check CHECK (requested_term_months >= 1),
    CONSTRAINT loan_applications_customer_id_fkey FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE RESTRICT,
    CONSTRAINT loan_applications_loan_product_id_fkey FOREIGN KEY (loan_product_id)
        REFERENCES loan_products (id) ON DELETE RESTRICT
);

CREATE INDEX idx_loan_applications_customer ON loan_applications (customer_id);
CREATE INDEX idx_loan_applications_loan_product ON loan_applications (loan_product_id);
CREATE INDEX idx_loan_applications_status ON loan_applications (status);
CREATE INDEX idx_loan_applications_deleted_at ON loan_applications (deleted_at);

COMMENT ON TABLE loan_applications IS 'A customer''s application for a specific loan product';

-- ----------------------------------------------------------------------------
-- loan_application_co_applicants
-- ----------------------------------------------------------------------------
CREATE TABLE loan_application_co_applicants (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id UUID         NOT NULL,
    customer_id         UUID         NOT NULL,
    relationship_type   VARCHAR(30)  NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_application_co_applicants_pkey PRIMARY KEY (id),
    CONSTRAINT loan_application_co_applicants_relationship_check CHECK (
        relationship_type IN ('spouse', 'family_member', 'business_partner', 'guarantor', 'other')
    ),
    CONSTRAINT loan_application_co_applicants_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT loan_application_co_applicants_customer_id_fkey FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE RESTRICT,
    CONSTRAINT loan_application_co_applicants_unique_key UNIQUE (loan_application_id, customer_id)
);

CREATE INDEX idx_loan_app_co_applicants_application ON loan_application_co_applicants (loan_application_id);
CREATE INDEX idx_loan_app_co_applicants_customer ON loan_application_co_applicants (customer_id);

-- ----------------------------------------------------------------------------
-- loan_application_incomes
-- ----------------------------------------------------------------------------
CREATE TABLE loan_application_incomes (
    id                   UUID          NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id  UUID          NOT NULL,
    income_type          VARCHAR(30)   NOT NULL,
    monthly_amount       NUMERIC(15,2) NOT NULL,
    source               VARCHAR(200),
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT loan_application_incomes_pkey PRIMARY KEY (id),
    CONSTRAINT loan_application_incomes_type_check CHECK (
        income_type IN ('salary', 'self_employment', 'rental', 'investment', 'pension', 'other')
    ),
    CONSTRAINT loan_application_incomes_amount_check CHECK (monthly_amount >= 0),
    CONSTRAINT loan_application_incomes_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_app_incomes_application ON loan_application_incomes (loan_application_id);

-- ----------------------------------------------------------------------------
-- loan_application_expenses
-- ----------------------------------------------------------------------------
CREATE TABLE loan_application_expenses (
    id                   UUID          NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id  UUID          NOT NULL,
    expense_type         VARCHAR(30)   NOT NULL,
    monthly_amount       NUMERIC(15,2) NOT NULL,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT loan_application_expenses_pkey PRIMARY KEY (id),
    CONSTRAINT loan_application_expenses_type_check CHECK (
        expense_type IN ('housing', 'existing_debt', 'living', 'dependents', 'other')
    ),
    CONSTRAINT loan_application_expenses_amount_check CHECK (monthly_amount >= 0),
    CONSTRAINT loan_application_expenses_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_app_expenses_application ON loan_application_expenses (loan_application_id);

-- ----------------------------------------------------------------------------
-- loan_application_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE loan_application_status_history (
    id                   UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id  UUID         NOT NULL,
    from_status          VARCHAR(20),
    to_status            VARCHAR(20)  NOT NULL,
    reason               VARCHAR(500),
    changed_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_application_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT loan_application_status_history_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_app_status_history_application ON loan_application_status_history (loan_application_id);

COMMENT ON TABLE loan_application_status_history IS 'Append-only audit trail of loan application status transitions';
