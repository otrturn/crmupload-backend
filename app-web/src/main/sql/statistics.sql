select *
from app.page_visits
order by visited desc;

select enabled, count(*)
from app.customer
group by enabled;

select 'crm-upload' as tag, status as status, count(*)
from app.crm_upload
group by status
union all
select 'duplicate-check' as tag, status as status, count(*)
from app.duplicate_check
group by status
order by tag, status;

SELECT
    count(*)
FROM app.customer_product cp
WHERE NOT EXISTS (
    SELECT 1
    FROM app.customer_invoice ci
    WHERE ci.customer_id = cp.customer_id
      AND COALESCE(ci.invoice_meta->'products', '[]'::jsonb)
        @> jsonb_build_array(jsonb_build_object('product', upper(cp.product)))
);