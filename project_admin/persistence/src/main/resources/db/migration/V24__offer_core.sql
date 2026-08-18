-- ============================================================================
-- V24__offer_core.sql
-- Offer domain core: loan_offers, loan_offer_status_history.
--
-- A loan offer presents an approved loan's final terms to the customer for
-- acceptance or decline. It references loan_applications and loan_approvals
-- — the approval it presents — the only other module it is coupled to.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- loan_offers
-- ----------------------------------------------------------------------------
CREATE TABLE loan_offers (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_application_id     UUID         NOT NULL,
    loan_approval_id        UUID         NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'issued',
    offered_amount          NUMERIC(15,2) NOT NULL,
    offered_term_months     INTEGER      NOT NULL,
    interest_rate           NUMERIC(6,3) NOT NULL,
    decline_reason          VARCHAR(1000),
    issued_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at              TIMESTAMPTZ  NOT NULL,
    accepted_at             TIMESTAMPTZ,
    declined_at             TIMESTAMPTZ,
    deleted_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_offers_pkey PRIMARY KEY (id),
    CONSTRAINT loan_offers_status_check CHECK (
        status IN ('issued', 'accepted', 'declined', 'expired')
    ),
    CONSTRAINT loan_offers_offered_amount_check CHECK (offered_amount > 0),
    CONSTRAINT loan_offers_offered_term_check CHECK (offered_term_months > 0),
    CONSTRAINT loan_offers_interest_rate_check CHECK (interest_rate >= 0),
    CONSTRAINT loan_offers_loan_application_id_fkey FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications (id) ON DELETE CASCADE,
    CONSTRAINT loan_offers_loan_approval_id_fkey FOREIGN KEY (loan_approval_id)
        REFERENCES loan_approvals (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_offers_loan_application ON loan_offers (loan_application_id);
CREATE INDEX idx_loan_offers_loan_approval ON loan_offers (loan_approval_id);
CREATE INDEX idx_loan_offers_status ON loan_offers (status);
CREATE INDEX idx_loan_offers_deleted_at ON loan_offers (deleted_at);
CREATE UNIQUE INDEX uq_loan_offers_loan_application ON loan_offers (loan_application_id) WHERE (deleted_at IS NULL);

COMMENT ON TABLE loan_offers IS 'One offer per loan application, presenting an approval''s terms to the customer for acceptance';

-- ----------------------------------------------------------------------------
-- loan_offer_status_history
-- ----------------------------------------------------------------------------
CREATE TABLE loan_offer_status_history (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    loan_offer_id       UUID         NOT NULL,
    from_status         VARCHAR(20),
    to_status           VARCHAR(20)  NOT NULL,
    reason              VARCHAR(500),
    changed_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT loan_offer_status_history_pkey PRIMARY KEY (id),
    CONSTRAINT loan_offer_status_history_offer_id_fkey FOREIGN KEY (loan_offer_id)
        REFERENCES loan_offers (id) ON DELETE CASCADE
);

CREATE INDEX idx_loan_offer_status_history_offer ON loan_offer_status_history (loan_offer_id);
