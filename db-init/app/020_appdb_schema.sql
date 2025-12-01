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
    id        INT PRIMARY KEY,
    username  TEXT NOT NULL UNIQUE,
    password  TEXT NOT NULL,
    roles     TEXT NOT NULL,
    lastlogin TIMESTAMPTZ
);

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
    email_address TEXT NOT NULL,
    password      TEXT NOT NULL,
    lastlogin     TIMESTAMPTZ
);

ALTER TABLE app.consumer
    ADD CONSTRAINT uq_consumer_consumer_id UNIQUE (consumer_id);

CREATE UNIQUE INDEX idx_consumer_email_address
    ON app.consumer (email_address);