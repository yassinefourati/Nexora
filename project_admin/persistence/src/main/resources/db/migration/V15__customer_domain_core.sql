-- ============================================================================
-- V15__customer_domain_core.sql
-- Customer domain core: customers, customer_addresses, customer_employments,
-- customer_identifications, customer_consents.
--
-- "Customer" here is the lending platform's loan applicant — distinct from
-- `users`, which is an internal/admin identity authenticated via Keycloak.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- customers
-- ----------------------------------------------------------------------------
CREATE TABLE customers (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    customer_type   VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'active',
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    business_name   VARCHAR(200),
    date_of_birth   DATE,
    national_id     VARCHAR(50),
    email           CITEXT       NOT NULL,
    phone           VARCHAR(30),
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT customers_pkey PRIMARY KEY (id),
    CONSTRAINT customers_customer_type_check CHECK (customer_type IN ('individual', 'business')),
    CONSTRAINT customers_status_check CHECK (status IN ('active', 'inactive', 'blocked')),
    CONSTRAINT customers_individual_name_check CHECK (
        customer_type <> 'individual' OR (first_name IS NOT NULL AND last_name IS NOT NULL)
    ),
    CONSTRAINT customers_business_name_check CHECK (
        customer_type <> 'business' OR business_name IS NOT NULL
    )
);

CREATE INDEX idx_customers_status ON customers (status);
CREATE INDEX idx_customers_deleted_at ON customers (deleted_at);
CREATE UNIQUE INDEX uq_customers_email ON customers (email) WHERE (deleted_at IS NULL);
CREATE UNIQUE INDEX uq_customers_national_id ON customers (national_id) WHERE (deleted_at IS NULL AND national_id IS NOT NULL);

COMMENT ON TABLE customers IS 'Loan platform customers (individual or business applicants)';

-- ----------------------------------------------------------------------------
-- customer_addresses
-- ----------------------------------------------------------------------------
CREATE TABLE customer_addresses (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    customer_id   UUID         NOT NULL,
    address_type  VARCHAR(20)  NOT NULL,
    line1         VARCHAR(200) NOT NULL,
    line2         VARCHAR(200),
    city          VARCHAR(100) NOT NULL,
    state         VARCHAR(100),
    postal_code   VARCHAR(20),
    country       VARCHAR(2)   NOT NULL,
    is_primary    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT customer_addresses_pkey PRIMARY KEY (id),
    CONSTRAINT customer_addresses_address_type_check CHECK (address_type IN ('current', 'previous', 'mailing')),
    CONSTRAINT customer_addresses_customer_id_fkey FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE CASCADE
);

CREATE INDEX idx_customer_addresses_customer ON customer_addresses (customer_id);

-- ----------------------------------------------------------------------------
-- customer_employments
-- ----------------------------------------------------------------------------
CREATE TABLE customer_employments (
    id                  UUID          NOT NULL DEFAULT gen_random_uuid(),
    customer_id         UUID          NOT NULL,
    employer_name       VARCHAR(200),
    job_title           VARCHAR(150),
    employment_status   VARCHAR(20)   NOT NULL,
    monthly_income      NUMERIC(15,2),
    start_date          DATE,
    end_date            DATE,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT customer_employments_pkey PRIMARY KEY (id),
    CONSTRAINT customer_employments_status_check CHECK (
        employment_status IN ('employed', 'self_employed', 'unemployed', 'retired')
    ),
    CONSTRAINT customer_employments_income_check CHECK (monthly_income IS NULL OR monthly_income >= 0),
    CONSTRAINT customer_employments_customer_id_fkey FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE CASCADE
);

CREATE INDEX idx_customer_employments_customer ON customer_employments (customer_id);

-- ----------------------------------------------------------------------------
-- customer_identifications
-- ----------------------------------------------------------------------------
CREATE TABLE customer_identifications (
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    customer_id       UUID         NOT NULL,
    id_type           VARCHAR(20)  NOT NULL,
    id_number         VARCHAR(100) NOT NULL,
    issuing_country   VARCHAR(2)   NOT NULL,
    issue_date        DATE,
    expiry_date       DATE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT customer_identifications_pkey PRIMARY KEY (id),
    CONSTRAINT customer_identifications_id_type_check CHECK (
        id_type IN ('passport', 'national_id', 'drivers_license')
    ),
    CONSTRAINT customer_identifications_customer_id_fkey FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE CASCADE
);

CREATE INDEX idx_customer_identifications_customer ON customer_identifications (customer_id);
CREATE UNIQUE INDEX uq_customer_identifications_type_number ON customer_identifications (customer_id, id_type, id_number);

-- ----------------------------------------------------------------------------
-- customer_consents
-- ----------------------------------------------------------------------------
CREATE TABLE customer_consents (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    customer_id     UUID         NOT NULL,
    consent_type    VARCHAR(30)  NOT NULL,
    granted         BOOLEAN      NOT NULL DEFAULT FALSE,
    granted_at      TIMESTAMPTZ,
    revoked_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT customer_consents_pkey PRIMARY KEY (id),
    CONSTRAINT customer_consents_consent_type_check CHECK (
        consent_type IN ('data_processing', 'credit_check', 'marketing')
    ),
    CONSTRAINT customer_consents_customer_id_fkey FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE CASCADE
);

CREATE INDEX idx_customer_consents_customer ON customer_consents (customer_id);
CREATE UNIQUE INDEX uq_customer_consents_customer_type ON customer_consents (customer_id, consent_type);
