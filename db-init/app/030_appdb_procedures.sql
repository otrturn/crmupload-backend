-- ------------------------------------------------------------
-- Billing
-- ------------------------------------------------------------

CREATE OR REPLACE PROCEDURE app.export_billing()
    LANGUAGE plpgsql
AS
$$
BEGIN
    WITH candidates AS (SELECT c.customer_id,
                               cu_first.modified        AS start_of_subscription,
                               cu_first.crm_customer_id AS crm_customer_id,
                               cu_first.source_system   AS source_system,
                               cu_first.crm_system      AS crm_system,
                               cu_first.crm_url         AS crm_url
                        FROM app.customer c
                                 JOIN LATERAL (
                            SELECT cu.crm_customer_id,
                                   cu.source_system,
                                   cu.crm_system,
                                   cu.crm_url,
                                   cu.modified
                            FROM app.crm_upload cu
                            WHERE cu.customer_id = c.customer_id
                              AND cu.status = 'done'
                            ORDER BY cu.modified ASC
                            LIMIT 1
                            ) cu_first ON TRUE
                        WHERE NOT EXISTS (SELECT 1
                                          FROM app.customer_billing cb
                                          WHERE cb.customer_id = c.customer_id
                                            AND cb.product = 'crm-upload'))
    INSERT
    INTO app.customer_billing (customer_id,
                               product,
                               status,
                               billing_meta,
                               start_of_subscription)
    SELECT customer_id,
           'crm-upload',
           'new-subscription',
           jsonb_build_object(
                   'crm_customer_id', crm_customer_id,
                   'source_system', source_system,
                   'crm_system', crm_system,
                   'crm_url', crm_url),
           start_of_subscription
    FROM candidates;

END;
$$;

