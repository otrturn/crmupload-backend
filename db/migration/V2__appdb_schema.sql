-- ****************************************************************************************************
-- user_account
-- ****************************************************************************************************
CREATE SEQUENCE app.sequence_user_account
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE app.user_account
(
    id        INT,
    username  TEXT NOT NULL UNIQUE,
    password  TEXT NOT NULL,
    roles     TEXT NOT NULL,
    lastlogin TIMESTAMPTZ
);

ALTER TABLE app.user_account
    ADD CONSTRAINT uq_user_account_id UNIQUE (id);

CREATE UNIQUE INDEX idx_user_account_username
    ON app.user_account (username);

-- ****************************************************************************************************
-- customer
-- ****************************************************************************************************
CREATE SEQUENCE app.sequence_customer
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS app.customer
(
    customer_id         INT         NOT NULL,
    customer_number     TEXT        NOT NULL,
    user_id             INT         NOT NULL,
    firstname           TEXT,
    lastname            TEXT,
    company_name        TEXT,
    email_address       TEXT        NOT NULL,
    phone_number        TEXT        NOT NULL,
    adrline1            TEXT        NOT NULL,
    adrline2            TEXT,
    postalcode          TEXT        NOT NULL,
    city                TEXT        NOT NULL,
    country             TEXT        NOT NULL CHECK (country IN ('DE', 'AT', 'CH')),
    tax_id              TEXT        NOT NULL,
    vat_id              TEXT        NOT NULL,
    enabled             BOOLEAN     NOT NULL DEFAULT false,
    billable            BOOLEAN     NOT NULL DEFAULT true,
    non_billable_reason jsonb,
    activation_date     TIMESTAMPTZ,
    under_observation   BOOLEAN     NOT NULL DEFAULT false,
    created             TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified            TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE app.customer
    ADD CONSTRAINT customer_person_or_company_chk
        CHECK (
            (
                firstname IS NOT NULL
                    AND lastname IS NOT NULL
                )
                OR
            company_name IS NOT NULL
            );

ALTER TABLE app.customer
    ADD CONSTRAINT uq_customer_customer_id UNIQUE (customer_id);

CREATE UNIQUE INDEX idx_customer_customer_number
    ON app.customer (customer_number);

CREATE UNIQUE INDEX idx_customer_email_address
    ON app.customer (email_address);

CREATE UNIQUE INDEX idx_customer_user_id
    ON app.customer (user_id);

ALTER TABLE app.customer
    ADD CONSTRAINT fk_customer_user_id
        FOREIGN KEY (user_id)
            REFERENCES app.user_account (id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- customer_acknowledgement
-- ****************************************************************************************************

CREATE TABLE IF NOT EXISTS app.customer_acknowledgement
(
    customer_id                     INT         NOT NULL,
    agb_accepted                    BOOLEAN     NOT NULL,
    is_entrepreneur                 BOOLEAN     NOT NULL,
    request_immediate_service_start BOOLEAN     NOT NULL,
    acknowledge_withdrawal_loss     BOOLEAN     NOT NULL,
    terms_version                   TEXT        NOT NULL,
    consents_at                     TIMESTAMPTZ NOT NULL DEFAULT now(),
    ip_address                      TEXT        NOT NULL,
    userAgent                       TEXT        NOT NULL,
    created                         TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified                        TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE app.customer_acknowledgement
    ADD CONSTRAINT fk_customer_acknowledgement_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- customer_activation
-- ****************************************************************************************************

CREATE TABLE IF NOT EXISTS app.customer_activation
(
    token       UUID        NOT NULL,
    customer_id INT         NOT NULL,
    created     TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ NOT NULL, -- z. B. now() + interval '24 hours'
    used        BOOLEAN     NOT NULL DEFAULT FALSE,
    used_at     TIMESTAMPTZ,
    CONSTRAINT pk_customer_activation
        PRIMARY KEY (token)
);

CREATE INDEX IF NOT EXISTS idx_customer_activation_customer_id
    ON app.customer_activation (customer_id);

ALTER TABLE app.customer_activation
    ADD CONSTRAINT fk_customer_activation_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- customer_product
-- ****************************************************************************************************
CREATE TABLE IF NOT EXISTS app.customer_product
(
    customer_id     INT         NOT NULL,
    product         TEXT        NOT NULL,
    enabled         BOOLEAN     NOT NULL DEFAULT false,
    activation_date TIMESTAMPTZ,
    created         TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified        TIMESTAMPTZ NOT NULL DEFAULT now()
        CONSTRAINT chk_customer_product_produc CHECK (product IN ('crm-upload', 'duplicate-check')),
    CONSTRAINT pk_customer_product
        PRIMARY KEY (customer_id, product)
);

CREATE INDEX idx_customer_product_customer_id
    ON app.customer_product (customer_id);

ALTER TABLE app.customer_product
    ADD CONSTRAINT fk_customer_product_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- customer_blocked
-- ****************************************************************************************************

CREATE TABLE IF NOT EXISTS app.customer_blocked
(
    customer_id INT         NOT NULL,
    created     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_customer_blocked
        PRIMARY KEY (customer_id)
);

CREATE INDEX IF NOT EXISTS idx_customer_blocked_customer_id
    ON app.customer_blocked (customer_id);

ALTER TABLE app.customer_blocked
    ADD CONSTRAINT fk_customer_blocked_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- customer_blocked_details
-- ****************************************************************************************************

CREATE TABLE IF NOT EXISTS app.customer_blocked_details
(
    customer_id  INT         NOT NULL,
    blocked_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    remark       TEXT        NOT NULL,
    resolution   TEXT,
    created      TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified     TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_customer_blocked_details_customer_id
    ON app.customer_blocked (customer_id);

ALTER TABLE app.customer_blocked_details
    ADD CONSTRAINT fk_customer_blocked_details_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- crm_upload
-- ****************************************************************************************************
CREATE SEQUENCE app.sequence_crm_upload
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS app.crm_upload
(
    upload_id       INT         NOT NULL,
    customer_id     INT         NOT NULL,
    source_system   TEXT,
    crm_system      TEXT,
    crm_customer_id TEXT,
    crm_url         TEXT,
    api_key         TEXT,
    content         BYTEA,
    statistics      jsonb       NOT NULL DEFAULT '{}'::jsonb,
    status          TEXT        NOT NULL DEFAULT 'new',
    is_test         BOOLEAN     NOT NULL DEFAULT FALSE,
    last_error      TEXT,
    created         TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_crm_upload_source_system CHECK (source_system IN ('Lexware', 'Bexio', 'MyExcel')),
    CONSTRAINT chk_crm_upload_crm_system CHECK (crm_system IN ('EspoCRM', 'Pipedrive')),
    CONSTRAINT chk_crm_upload_status CHECK (status IN ('new', 'processing', 'done', 'failed'))
);

ALTER TABLE app.crm_upload
    ADD CONSTRAINT uq_crm_upload_upload_id UNIQUE (upload_id);

CREATE INDEX idx_crm_upload_customer_id
    ON app.crm_upload (customer_id);

ALTER TABLE app.crm_upload
    ADD CONSTRAINT fk_crm_upload_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- crm_upload_observation
-- ****************************************************************************************************
CREATE TABLE IF NOT EXISTS app.crm_upload_observation
(
    upload_id       INT         NOT NULL,
    customer_id     INT         NOT NULL,
    source_system   TEXT,
    crm_system      TEXT,
    crm_customer_id TEXT,
    crm_url         TEXT,
    api_key         TEXT,
    content         BYTEA,
    status          TEXT        NOT NULL DEFAULT 'new',
    is_test         BOOLEAN,
    created         TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_crm_upload_observation_source_system CHECK (source_system IN ('Lexware', 'Bexio', 'MyExcel')),
    CONSTRAINT chk_crm_upload_observation_crm_system CHECK (crm_system IN ('EspoCRM', 'Pipedrive')),
    CONSTRAINT chk_crm_upload_observation_status CHECK (status IN ('new', 'processing', 'done', 'failed'))
);

ALTER TABLE app.crm_upload_observation
    ADD CONSTRAINT uq_crm_upload_observation_upload_id UNIQUE (upload_id);

CREATE INDEX idx_crm_upload_observation_customer_id
    ON app.crm_upload_observation (customer_id);

ALTER TABLE app.crm_upload_observation
    ADD CONSTRAINT fk_crm_upload_observation_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

ALTER TABLE app.crm_upload_observation
    ADD CONSTRAINT fk_crm_upload_observation_upload_id
        FOREIGN KEY (upload_id)
            REFERENCES app.crm_upload (upload_id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- duplicate_check
-- ****************************************************************************************************
CREATE SEQUENCE app.sequence_duplicate_check
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS app.duplicate_check
(
    duplicate_check_id INT         NOT NULL,
    customer_id        INT         NOT NULL,
    source_system      TEXT,
    content            BYTEA,
    statistics         jsonb       NOT NULL DEFAULT '{}'::jsonb,
    status             TEXT        NOT NULL DEFAULT 'new',
    is_test            BOOLEAN     NOT NULL DEFAULT FALSE,
    last_error         TEXT,
    created            TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_duplicate_check_source_system CHECK (source_system IN ('Lexware', 'Bexio', 'MyExcel')),
    CONSTRAINT chk_duplicate_check_status CHECK (status IN ('new', 'verifying', 'verified', 'duplicate-checking',
                                                            'duplicate-checked', 'finalising', 'done', 'failed'))
);

ALTER TABLE app.duplicate_check
    ADD CONSTRAINT uq_duplicate_check_duplicate_check_id UNIQUE (duplicate_check_id);

CREATE INDEX idx_duplicate_check_customer_id
    ON app.duplicate_check (customer_id);

ALTER TABLE app.duplicate_check
    ADD CONSTRAINT fk_duplicate_check_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- duplicate_check_observation
-- ****************************************************************************************************
CREATE TABLE IF NOT EXISTS app.duplicate_check_observation
(
    duplicate_check_id INT         NOT NULL,
    customer_id        INT         NOT NULL,
    source_system      TEXT,
    content            BYTEA,
    status             TEXT        NOT NULL DEFAULT 'new',
    is_test            BOOLEAN     NOT NULL DEFAULT FALSE,
    created            TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_duplicate_check_observation_source_system CHECK (source_system IN ('Lexware', 'Bexio', 'MyExcel')),
    CONSTRAINT chk_duplicate_check_observation_status CHECK (status IN
                                                             ('new', 'verifying', 'verified', 'duplicate-checking',
                                                              'duplicate-checked', 'finalising', 'done', 'failed'))
);

ALTER TABLE app.duplicate_check_observation
    ADD CONSTRAINT uq_duplicate_check_observation_duplicate_check_id UNIQUE (duplicate_check_id);

CREATE INDEX idx_duplicate_checkk_observation_customer_id
    ON app.duplicate_check_observation (customer_id);

ALTER TABLE app.duplicate_check_observation
    ADD CONSTRAINT fk_duplicate_check_observation_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

ALTER TABLE app.duplicate_check_observation
    ADD CONSTRAINT fk_duplicate_check_observation_duplicate_check_id
        FOREIGN KEY (duplicate_check_id)
            REFERENCES app.duplicate_check (duplicate_check_id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- customer_verification_task
-- ****************************************************************************************************
CREATE SEQUENCE app.sequence_customer_verification_task
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS app.customer_verification_task
(
    verification_task_id        INT         NOT NULL,
    customer_id                 INT         NOT NULL,
    task_description            TEXT        NOT NULL,
    task_resolution_description TEXT,
    task_resolution_date        TIMESTAMPTZ,
    created                     TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified                    TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE app.customer_verification_task
    ADD CONSTRAINT uq_customer_verification_task_verification_id UNIQUE (verification_task_id);

ALTER TABLE app.customer_verification_task
    ADD CONSTRAINT fk_customer_verification_task_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

CREATE INDEX idx_customer_verification_task_customer_id
    ON app.customer_verification_task (customer_id);

-- ****************************************************************************************************
-- customer_invoice
-- ****************************************************************************************************
CREATE SEQUENCE app.sequence_customer_invoice
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS app.customer_invoice
(
    invoice_id           INT         NOT NULL,
    invoice_no           TEXT        NOT NULL,
    customer_id          INT         NOT NULL,
    invoice_date         TIMESTAMPTZ NOT NULL,
    invoice_due_date     TIMESTAMPTZ NOT NULL,
    invoice_mailing_date TIMESTAMPTZ,
    invoice_payment_date TIMESTAMPTZ,
    invoice_agency_date  TIMESTAMPTZ,
    invoice_meta         jsonb       NOT NULL DEFAULT '{}'::jsonb,
    invoice_image        BYTEA       NOT NULL,
    invoice_pdf_name     TEXT        NOT NULL,
    tax_value            NUMERIC     NOT NULL,
    tax_amount           NUMERIC     NOT NULL,
    net_amount           NUMERIC     NOT NULL,
    amount               NUMERIC     NOT NULL,
    cancelled            BOOLEAN     NOT NULL DEFAULT false,
    cancelled_reason     jsonb,
    created              TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified             TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE app.customer_invoice
    ADD CONSTRAINT uq_customer_invoice_invoice_id UNIQUE (invoice_id);

ALTER TABLE app.customer_invoice
    ADD CONSTRAINT fk_customer_invoice_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_customer_invoice_products
    ON app.customer_invoice
        USING gin ((invoice_meta -> 'products'));

-- ****************************************************************************************************
-- page_visits
-- ****************************************************************************************************

CREATE TABLE IF NOT EXISTS app.page_visits
(
    page_id TEXT        NOT NULL,
    visited TIMESTAMPTZ NOT NULL DEFAULT now()
);
