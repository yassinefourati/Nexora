-- ============================================================================
-- V25__contract_core.sql
-- Contract domain core: loan_contracts, loan_contract_status_history.
--
-- A loan contract is generated from an accepted loan_offer, carrying the
-- final terms into a document the customer will sign. Signature capture
-- itself belongs to a later Signature module — this one only tracks the
-- contract document's own lifecycle (draft -> finalized -> cancelled).
-- ============================================================================

-- ----------------------------------------------------------------------------
-- loan_contracts
-- ----------------------------------------------------------------------------
CREATE TABLE loan_contracts (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id     UUID         NOT NULL,
    loan_offer_id           UUID         NOT NULL,
    contract_number         VARCHAR(50)  NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'draft',
    principal_amount        NUMERIC(15,2) NOT NULL,
    term_months             INTEGER      NOT NULL,
    interest_rate           NUMERIC(6,3) NOT NULL,
    document_url            VARCHAR(500),
    finalized_at            TIMESTAMPTZ,
    cancelled_at            TIMESTAMPTZ,
    cancellation_reason     VARCHAR(1000),
    deleted_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_contracts_pkey PRIMARY KEY (id),
    CONSTRAINT loan_contracts_status_check CHECK (
        status IN ('draft', 'finalized', 'cancelled')
    ),
    CONSTRAINT loan_contracts_principal_amount_check CHECK (principal_amount > 0),
    CONSTRAINT loan_contracts_term_months_check CHECK (term_months > 0),
    CONSTRAINT loan_contracts_interest_rate_check CHECK (interest_rate >= 0),
    CONSTRAINT loan_contracts_loan_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT loan_contracts_loan_offer_id_fkey FOREIGN KEY (loan_offer_id)
        REFERENCES loan_offers (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_contracts_loan_application ON loan_contracts (loan_application_id);
CREATE INDEX idx_loan_contracts_loan_offer ON loan_contracts (loan_offer_id);
CREATE INDEX idx_loan_contracts_status ON loan_contracts (status);
CREATE INDEX idx_loan_contracts_deleted_at ON loan_contracts (deleted_at);
CREATE UNIQUE INDEX uq_loan_contracts_loan_application ON loan_contracts (loan_application_id) WHERE (deleted_at IS NULL);
CREATE UNIQUE INDEX uq_loan_contracts_contract_number ON loan_contracts (contract_number) WHERE (deleted_at IS NULL);

COMMENT ON TABLE loan_contracts IS 'One contract document per loan application, generated from an accepted loan offer';

-- ----------------------------------------------------------------------------
-- loan_contract_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE loan_contract_status_history (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_contract_id    UUID         NOT NULL,
    from_status         VARCHAR(20),
    to_status           VARCHAR(20)  NOT NULL,
    reason              VARCHAR(500),
    changed_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_contract_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT loan_contract_status_history_contract_id_fkey FOREIGN KEY (loan_contract_id)
        REFERENCES loan_contracts (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_contract_status_history_contract ON loan_contract_status_history (loan_contract_id);
