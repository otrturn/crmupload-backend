select *
from app.page_visits
order by visited desc;

delete
from app.page_visits;
commit;

select *
from app.user_account;

select *
from app.customer;

select *
from app.crm_upload;

select *
from app.customer_product
order by customer_id;

select *
from app.customer_billing
order by customer_id;

delete
from app.customer_billing;
commit;

delete
from app.crm_upload;
commit;

call app.export_billing();

update app.customer
set enabled= true;
commit;

update app.crm_upload
set status= 'new';
commit;

SELECT cu.created         AS ts,
       cu.source_system   AS source_system,
       cu.crm_system      AS crm_system,
       cu.crm_customer_id AS crm_customer_id,
       cu.status          AS status
FROM app.crm_upload cu
         JOIN app.customer c
              ON c.customer_id = cu.customer_id
WHERE c.email_address = 'ralf+0@test.de'
ORDER BY cu.created DESC;

update app.customer_billing
set submitted_to_billing=null;
commit;

SELECT customer_id
FROM app.customer_billing
WHERE submitted_to_billing IS NULL
  AND product = 'crm-upload'
  AND status = 'new-subscription'
    FOR UPDATE SKIP LOCKED;