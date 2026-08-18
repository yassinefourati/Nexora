-- ============================================================================
-- V27__disbursement_core.sql
-- Disbursement domain core: loan_disbursements, loan_disbursement_status_history.
--
-- A disbursement releases the principal funds for a loan_application once
-- its loan_contract is fully signed. References loan_applications and
-- loan_contracts — the contract it disburses against — the only other
-- module it is coupled to; signature completeness is checked by the
-- service layer via contract_signatures rather than an FK.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- loan_disbursements
-- ----------------------------------------------------------------------------
CREATE TABLE loan_disbursements (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id     UUID         NOT NULL,
    loan_contract_id        UUID         NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'pending',
    amount                  NUMERIC(15,2) NOT NULL,
    disbursement_method     VARCHAR(20)  NOT NULL,
    destination_account     VARCHAR(100) NOT NULL,
    reference_number        VARCHAR(100),
    failure_reason          VARCHAR(1000),
    initiated_at            TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    failed_at               TIMESTAMPTZ,
    deleted_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_disbursements_pkey PRIMARY KEY (id),
    CONSTRAINT loan_disbursements_status_check CHECK (
        status IN ('pending', 'initiated', 'completed', 'failed')
    ),
    CONSTRAINT loan_disbursements_method_check CHECK (
        disbursement_method IN ('bank_transfer', 'check', 'wire')
    ),
    CONSTRAINT loan_disbursements_amount_check CHECK (amount > 0),
    CONSTRAINT loan_disbursements_loan_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT loan_disbursements_loan_contract_id_fkey FOREIGN KEY (loan_contract_id)
        REFERENCES loan_contracts (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_disbursements_loan_application ON loan_disbursements (loan_application_id);
CREATE INDEX idx_loan_disbursements_loan_contract ON loan_disbursements (loan_contract_id);
CREATE INDEX idx_loan_disbursements_status ON loan_disbursements (status);
CREATE INDEX idx_loan_disbursements_deleted_at ON loan_disbursements (deleted_at);
CREATE UNIQUE INDEX uq_loan_disbursements_loan_application ON loan_disbursements (loan_application_id) WHERE (deleted_at IS NULL);

COMMENT ON TABLE loan_disbursements IS 'One disbursement per loan application, releasing principal funds once the contract is fully signed';

-- ----------------------------------------------------------------------------
-- loan_disbursement_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE loan_disbursement_status_history (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_disbursement_id    UUID         NOT NULL,
    from_status             VARCHAR(20),
    to_status               VARCHAR(20)  NOT NULL,
    reason                  VARCHAR(500),
    changed_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_disbursement_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT loan_disbursement_status_history_disbursement_id_fkey FOREIGN KEY (loan_disbursement_id)
        REFERENCES loan_disbursements (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_disbursement_status_history_disbursement ON loan_disbursement_status_history (loan_disbursement_id);
