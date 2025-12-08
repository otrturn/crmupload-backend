-- ------------------------------------------------------------
-- Clear all accounts
-- ------------------------------------------------------------

CREATE OR REPLACE PROCEDURE app.clearAccounts()
    LANGUAGE plpgsql
AS
$$
BEGIN
    TRUNCATE TABLE app.customer_activation CASCADE;
    TRUNCATE TABLE app.customer_upload CASCADE;
    TRUNCATE TABLE app.customer CASCADE;
    TRUNCATE TABLE app.user_account CASCADE;
    COMMIT;
END;
$$;

-- ------------------------------------------------------------
-- Billing
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION app.export_billing()
    RETURNS TABLE
            (
                firstname     TEXT,
                lastname      TEXT,
                company_name  TEXT,
                email_address TEXT,
                phone_number  TEXT,
                adrline1      TEXT,
                adrline2      TEXT,
                postalcode    TEXT,
                city          TEXT,
                country       TEXT,
                source_system TEXT,
                crm_system    TEXT
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        WITH candidates AS (SELECT c.customer_id,
                                   c.firstname,
                                   c.lastname,
                                   c.company_name,
                                   c.email_address,
                                   c.phone_number,
                                   c.adrline1,
                                   c.adrline2,
                                   c.postalcode,
                                   c.city,
                                   c.country,
                                   cu_first.source_system,
                                   cu_first.crm_system,
                                   cu_first.modified AS start_of_subscription
                            FROM app.customer c
                                     -- ältester "done"-Upload je Customer
                                     JOIN LATERAL (
                                SELECT cu.source_system,
                                       cu.crm_system,
                                       cu.modified
                                FROM app.customer_upload cu
                                WHERE cu.customer_id = c.customer_id
                                  AND cu.status = 'done'
                                ORDER BY cu.modified ASC
                                LIMIT 1
                                ) cu_first ON TRUE
                            -- nur Customer ohne Eintrag in customer_billing
                            WHERE NOT EXISTS (SELECT 1
                                              FROM app.customer_billing cb
                                              WHERE cb.customer_id = c.customer_id)),
             inserted AS (
                 INSERT INTO app.customer_billing (
                                                   customer_id,
                                                   status,
                                                   start_of_subscription,
                                                   submitted_to_billing
                     )
                     SELECT customer_id,
                            'new',
                            start_of_subscription,
                            now()
                     FROM candidates
                     RETURNING customer_id)
        SELECT c.firstname,
               c.lastname,
               c.company_name,
               c.email_address,
               c.phone_number,
               c.adrline1,
               c.adrline2,
               c.postalcode,
               c.city,
               c.country,
               c.source_system,
               c.crm_system
        FROM candidates c
                 JOIN inserted i
                      ON i.customer_id = c.customer_id;
END;
$$;
