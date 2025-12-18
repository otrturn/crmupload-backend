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

select upload_id, customer_id, source_system, crm_system, status
from app.crm_upload;

select duplicate_check_id, customer_id, source_system, status
from app.duplicate_check
order by duplicate_check_id desc;

select duplicate_check_id, customer_id, source_system, status
from app.duplicate_check_observation
order by duplicate_check_id desc;

select *
from app.customer_product
order by customer_id;

select *
from app.customer_billing
order by customer_id;

update app.customer
set enabled= true,
    activation_date=now();
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
