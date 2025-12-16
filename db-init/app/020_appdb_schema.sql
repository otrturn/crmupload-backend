DROP TABLE IF EXISTS app.page_visits CASCADE;

DROP TABLE IF EXISTS app.duplicate_check CASCADE;
DROP SEQUENCE IF EXISTS app.sequence_duplicate_check;

DROP TABLE IF EXISTS app.customer_billing CASCADE;
DROP TABLE IF EXISTS app.customer_activation CASCADE;

DROP TABLE IF EXISTS app.crm_upload CASCADE;
DROP SEQUENCE IF EXISTS app.sequence_crm_upload;

DROP TABLE IF EXISTS app.customer_product CASCADE;

DROP TABLE IF EXISTS app.customer CASCADE;
DROP SEQUENCE IF EXISTS app.sequence_customer;

DROP TABLE IF EXISTS app.user_account CASCADE;
DROP SEQUENCE IF EXISTS app.sequence_user_account;

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
    customer_id     INT         NOT NULL,
    customer_number TEXT        NOT NULL,
    user_id         INT         NOT NULL,
    firstname       TEXT,
    lastname        TEXT,
    company_name    TEXT,
    email_address   TEXT        NOT NULL,
    phone_number    TEXT        NOT NULL,
    adrline1        TEXT        NOT NULL,
    adrline2        TEXT,
    postalcode      TEXT        NOT NULL,
    city            TEXT        NOT NULL,
    country         TEXT        NOT NULL CHECK (country IN ('DE', 'AT', 'CH')),
    enabled         BOOLEAN     NOT NULL DEFAULT false,
    activation_date TIMESTAMPTZ,
    created         TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified        TIMESTAMPTZ NOT NULL DEFAULT now()
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
-- customer_activation
-- ****************************************************************************************************

CREATE TABLE IF NOT EXISTS app.customer_activation
(
    token       UUID        NOT NULL PRIMARY KEY,
    customer_id INT         NOT NULL REFERENCES app.customer (customer_id),
    created     TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ NOT NULL, -- z. B. now() + interval '24 hours'
    used        BOOLEAN     NOT NULL DEFAULT FALSE,
    used_at     TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_customer_activation_customer
    ON app.customer_activation (customer_id);

-- ****************************************************************************************************
-- customer_product
-- ****************************************************************************************************
CREATE TABLE IF NOT EXISTS app.customer_product
(
    customer_id INT  NOT NULL,
    product     TEXT NOT NULL,
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
    crm_customer_id TEXT,
    source_system   TEXT,
    crm_system      TEXT,
    crm_url         TEXT,
    api_key         TEXT,
    content         BYTEA,
    status          TEXT        NOT NULL DEFAULT 'new',
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
    status             TEXT        NOT NULL DEFAULT 'new',
    last_error         TEXT,
    created            TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_duplicate_check_source_system CHECK (source_system IN ('Lexware', 'Bexio', 'MyExcel')),
    CONSTRAINT chk_duplicate_check_status CHECK (status IN ('new', 'verifying', 'verified', 'duplicate-checking',
                                                            'duplicate-checked', 'finalising', 'done', 'failed'))
);

ALTER TABLE app.duplicate_check
    ADD CONSTRAINT uq_duplicate_check_upload_id UNIQUE (duplicate_check_id);

CREATE INDEX idx_duplicate_check_customer_id
    ON app.duplicate_check (customer_id);

ALTER TABLE app.duplicate_check
    ADD CONSTRAINT fk_duplicate_check_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- customer_billing
-- ****************************************************************************************************

CREATE TABLE IF NOT EXISTS app.customer_billing
(
    customer_id           INT         NOT NULL,
    product               TEXT        NOT NULL,
    status                TEXT        NOT NULL DEFAULT 'new-subscription',
    billing_meta          jsonb       NOT NULL DEFAULT '{}'::jsonb,
    start_of_subscription TIMESTAMPTZ,
    submitted_to_billing  TIMESTAMPTZ,
    created               TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_customer_billing_product CHECK (product IN ('crm-upload', 'duplicates')),
    CONSTRAINT chk_customer_billing_status CHECK (status IN ('new-subscription', 'renewal'))
);

ALTER TABLE app.customer_billing
    ADD CONSTRAINT fk_customer_billing_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES app.customer (customer_id)
            ON DELETE RESTRICT;

-- ****************************************************************************************************
-- page_visits
-- ****************************************************************************************************

CREATE TABLE IF NOT EXISTS app.page_visits
(
    page_id TEXT        NOT NULL,
    visited TIMESTAMPTZ NOT NULL DEFAULT now()
);
