-- ------------------------------------------------------------
-- Clear all accounts
-- ------------------------------------------------------------

CREATE OR REPLACE PROCEDURE rag.clearAccounts()
    LANGUAGE plpgsql
AS
$$
BEGIN
    TRUNCATE TABLE app.consumer_activation CASCADE;
    TRUNCATE TABLE app.consumer_upload CASCADE;
    TRUNCATE TABLE app.consumer CASCADE;
    TRUNCATE TABLE app.user_account CASCADE;
    COMMIT;
END;
$$;