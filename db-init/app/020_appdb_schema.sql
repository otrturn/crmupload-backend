DROP TABLE IF EXISTS app.consumer CASCADE;
DROP SEQUENCE IF EXISTS app.sequence_consumer;

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
-- consumer
-- ****************************************************************************************************
CREATE SEQUENCE app.sequence_consumer
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS app.consumer
(
    consumer_id   INT  NOT NULL,
    user_id       INT  NOT NULL,
    firstname     TEXT NOT NULL,
    lastname      TEXT NOT NULL,
    email_address TEXT NOT NULL,
    phone_number  TEXT NOT NULL,
    adrline1      TEXT NOT NULL,
    adrline2      TEXT,
    postalcode    TEXT NOT NULL,
    city          TEXT NOT NULL,
    country       TEXT NOT NULL
);

ALTER TABLE app.consumer
    ADD CONSTRAINT uq_consumer_consumer_id UNIQUE (consumer_id);

CREATE UNIQUE INDEX idx_consumer_email_address
    ON app.consumer (email_address);

CREATE UNIQUE INDEX idx_consumer_user_id
    ON app.consumer (user_id);

ALTER TABLE app.consumer
    ADD CONSTRAINT fk_consumer_user_id
        FOREIGN KEY (user_id)
            REFERENCES app.user_account (id)
            ON DELETE RESTRICT;