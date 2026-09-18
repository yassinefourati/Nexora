-- ============================================================================
-- V28__servicing_core.sql
-- Loan servicing domain core: loan_accounts, loan_installments,
-- loan_account_status_history.
--
-- A loan account is opened once a disbursement completes, carrying the
-- funded loan into its repayment life. Its reducing-balance installment
-- schedule is generated at opening time. Payment capture against
-- installments belongs to a later Repayment module — this one only tracks
-- the account and the schedule it owes against.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- loan_accounts
-- ----------------------------------------------------------------------------
CREATE TABLE loan_accounts (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id     UUID         NOT NULL,
    loan_disbursement_id    UUID         NOT NULL,
    account_number          VARCHAR(50)  NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'active',
    principal_amount        NUMERIC(15,2) NOT NULL,
    interest_rate           NUMERIC(6,3) NOT NULL,
    term_months             INTEGER      NOT NULL,
    outstanding_principal   NUMERIC(15,2) NOT NULL,
    opened_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    closed_at               TIMESTAMPTZ,
    deleted_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_accounts_pkey PRIMARY KEY (id),
    CONSTRAINT loan_accounts_status_check CHECK (
        status IN ('active', 'closed', 'defaulted')
    ),
    CONSTRAINT loan_accounts_principal_amount_check CHECK (principal_amount > 0),
    CONSTRAINT loan_accounts_interest_rate_check CHECK (interest_rate >= 0),
    CONSTRAINT loan_accounts_term_months_check CHECK (term_months > 0),
    CONSTRAINT loan_accounts_outstanding_principal_check CHECK (outstanding_principal >= 0),
    CONSTRAINT loan_accounts_loan_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT loan_accounts_loan_disbursement_id_fkey FOREIGN KEY (loan_disbursement_id)
        REFERENCES loan_disbursements (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_accounts_loan_application ON loan_accounts (loan_application_id);
CREATE INDEX idx_loan_accounts_loan_disbursement ON loan_accounts (loan_disbursement_id);
CREATE INDEX idx_loan_accounts_status ON loan_accounts (status);
CREATE INDEX idx_loan_accounts_deleted_at ON loan_accounts (deleted_at);
CREATE UNIQUE INDEX uq_loan_accounts_loan_application ON loan_accounts (loan_application_id) WHERE (deleted_at IS NULL);
CREATE UNIQUE INDEX uq_loan_accounts_account_number ON loan_accounts (account_number) WHERE (deleted_at IS NULL);

COMMENT ON TABLE loan_accounts IS 'One servicing account per loan application, opened once its disbursement completes';

-- ----------------------------------------------------------------------------
-- loan_installments
-- ----------------------------------------------------------------------------
CREATE TABLE loan_installments (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_account_id     UUID         NOT NULL,
    installment_number  INTEGER      NOT NULL,
    due_date            DATE         NOT NULL,
    principal_amount    NUMERIC(15,2) NOT NULL,
    interest_amount     NUMERIC(15,2) NOT NULL,
    total_amount        NUMERIC(15,2) NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'pending',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_installments_pkey PRIMARY KEY (id),
    CONSTRAINT loan_installments_status_check CHECK (
        status IN ('pending', 'paid', 'overdue')
    ),
    CONSTRAINT loan_installments_principal_check CHECK (principal_amount >= 0),
    CONSTRAINT loan_installments_interest_check CHECK (interest_amount >= 0),
    CONSTRAINT loan_installments_total_check CHECK (total_amount >= 0),
    CONSTRAINT loan_installments_account_id_fkey FOREIGN KEY (loan_account_id)
        REFERENCES loan_accounts (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_installments_account ON loan_installments (loan_account_id);
CREATE INDEX idx_loan_installments_status ON loan_installments (status);
CREATE UNIQUE INDEX uq_loan_installments_account_number ON loan_installments (loan_account_id, installment_number);

-- ----------------------------------------------------------------------------
-- loan_account_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE loan_account_status_history (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_account_id     UUID         NOT NULL,
    from_status         VARCHAR(20),
    to_status           VARCHAR(20)  NOT NULL,
    reason              VARCHAR(500),
    changed_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_account_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT loan_account_status_history_account_id_fkey FOREIGN KEY (loan_account_id)
        REFERENCES loan_accounts (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_account_status_history_account ON loan_account_status_history (loan_account_id);
