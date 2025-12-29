CREATE OR REPLACE FUNCTION app.kpi_open_products_revenue()
    RETURNS TABLE
            (
                customers_1_product  bigint,
                customers_2_products bigint,
                revenue_1_product    bigint,
                revenue_2_products   bigint,
                total_revenue        bigint
            )
    LANGUAGE sql
    STABLE
AS
$$
WITH price_map AS (SELECT 1 AS product_count, 250 AS price
                   UNION ALL
                   SELECT 2, 400),
     customer_open_products AS (SELECT cp.customer_id,
                                       COUNT(*) AS open_product_count
                                FROM app.customer_product cp
                                         JOIN app.customer c
                                              ON c.customer_id = cp.customer_id
                                                  AND c.activation_date IS NOT NULL
                                                  AND c.billable IS TRUE
                                WHERE NOT EXISTS (SELECT 1
                                                  FROM app.customer_invoice ci
                                                  WHERE ci.customer_id = cp.customer_id
                                                    AND COALESCE(ci.invoice_meta -> 'products', '[]'::jsonb)
                                                      @> jsonb_build_array(
                                                                jsonb_build_object('product', upper(cp.product))
                                                         ))
                                GROUP BY cp.customer_id),
     rated AS (SELECT cop.customer_id,
                      cop.open_product_count AS product_count,
                      pm.price
               FROM customer_open_products cop
                        LEFT JOIN price_map pm
                                  ON pm.product_count = cop.open_product_count)
SELECT COALESCE(SUM(CASE WHEN product_count = 1 THEN 1 ELSE 0 END), 0)::bigint AS customers_1_product,
       COALESCE(SUM(CASE WHEN product_count = 2 THEN 1 ELSE 0 END), 0)::bigint AS customers_2_products,
       COALESCE(SUM(price) FILTER (WHERE product_count = 1), 0)::bigint        AS revenue_1_product,
       COALESCE(SUM(price) FILTER (WHERE product_count = 2), 0)::bigint        AS revenue_2_products,
       COALESCE(SUM(price), 0)::bigint                                         AS total_revenue
FROM rated;
$$;
