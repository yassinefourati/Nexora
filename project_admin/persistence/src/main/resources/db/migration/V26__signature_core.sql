-- ============================================================================
-- V26__signature_core.sql
-- Signature domain core: contract_signatures, contract_signature_status_history.
--
-- A contract may require more than one signature (primary applicant plus
-- co-applicants), so contract_signatures is a one-to-many child of
-- loan_contracts rather than a 1:1 extension — each row tracks one signer's
-- signing request through to signed/declined.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- contract_signatures
-- ----------------------------------------------------------------------------
CREATE TABLE contract_signatures (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_contract_id    UUID         NOT NULL,
    signer_name         VARCHAR(200) NOT NULL,
    signer_role         VARCHAR(20)  NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'pending',
    signature_method    VARCHAR(20)  NOT NULL DEFAULT 'electronic',
    decline_reason      VARCHAR(1000),
    requested_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    signed_at           TIMESTAMPTZ,
    declined_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT contract_signatures_pkey PRIMARY KEY (id),
    CONSTRAINT contract_signatures_signer_role_check CHECK (
        signer_role IN ('primary_applicant', 'co_applicant', 'guarantor')
    ),
    CONSTRAINT contract_signatures_status_check CHECK (
        status IN ('pending', 'signed', 'declined')
    ),
    CONSTRAINT contract_signatures_signature_method_check CHECK (
        signature_method IN ('electronic', 'wet_ink')
    ),
    CONSTRAINT contract_signatures_loan_contract_id_fkey FOREIGN KEY (loan_contract_id)
        REFERENCES loan_contracts (id) ON DELETE CASCADE
);

CREATE INDEX idx_contract_signatures_contract ON contract_signatures (loan_contract_id);
CREATE INDEX idx_contract_signatures_status ON contract_signatures (status);

COMMENT ON TABLE contract_signatures IS 'One row per signer required on a loan contract (primary applicant, co-applicants, guarantors)';

-- ----------------------------------------------------------------------------
-- contract_signature_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE contract_signature_status_history (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    contract_signature_id   UUID         NOT NULL,
    from_status             VARCHAR(20),
    to_status               VARCHAR(20)  NOT NULL,
    reason                  VARCHAR(500),
    changed_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT contract_signature_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT contract_signature_status_history_signature_id_fkey FOREIGN KEY (contract_signature_id)
        REFERENCES contract_signatures (id) ON DELETE CASCADE
);

CREATE INDEX idx_contract_signature_status_history_signature ON contract_signature_status_history (contract_signature_id);
