-- ============================================================================
-- V29__repayment_core.sql
-- Repayment domain core: loan_repayments, loan_repayment_status_history.
--
-- A repayment captures a customer payment against one specific
-- loan_installment. References loan_accounts and loan_installments — the
-- installment it pays off — the only other module it is coupled to; on
-- completion the service layer marks the installment paid and reduces the
-- account's outstanding_principal rather than this table holding that
-- state itself.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- loan_repayments
-- ----------------------------------------------------------------------------
CREATE TABLE loan_repayments (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_account_id         UUID         NOT NULL,
    loan_installment_id     UUID         NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'pending',
    amount                  NUMERIC(15,2) NOT NULL,
    payment_method          VARCHAR(20)  NOT NULL,
    reference_number        VARCHAR(100),
    failure_reason          VARCHAR(1000),
    paid_at                 TIMESTAMPTZ,
    failed_at               TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_repayments_pkey PRIMARY KEY (id),
    CONSTRAINT loan_repayments_status_check CHECK (
        status IN ('pending', 'completed', 'failed')
    ),
    CONSTRAINT loan_repayments_method_check CHECK (
        payment_method IN ('bank_transfer', 'card', 'cash')
    ),
    CONSTRAINT loan_repayments_amount_check CHECK (amount > 0),
    CONSTRAINT loan_repayments_account_id_fkey FOREIGN KEY (loan_account_id)
        REFERENCES loan_accounts (id) ON DELETE CASCADE,
    CONSTRAINT loan_repayments_installment_id_fkey FOREIGN KEY (loan_installment_id)
        REFERENCES loan_installments (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_repayments_account ON loan_repayments (loan_account_id);
CREATE INDEX idx_loan_repayments_installment ON loan_repayments (loan_installment_id);
CREATE INDEX idx_loan_repayments_status ON loan_repayments (status);

COMMENT ON TABLE loan_repayments IS 'Customer payments captured against a specific loan_installment';

-- ----------------------------------------------------------------------------
-- loan_repayment_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE loan_repayment_status_history (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_repayment_id       UUID         NOT NULL,
    from_status             VARCHAR(20),
    to_status               VARCHAR(20)  NOT NULL,
    reason                  VARCHAR(500),
    changed_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_repayment_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT loan_repayment_status_history_repayment_id_fkey FOREIGN KEY (loan_repayment_id)
        REFERENCES loan_repayments (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_repayment_status_history_repayment ON loan_repayment_status_history (loan_repayment_id);
