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

select upload_id, customer_id, source_system, crm_system, status
from app.crm_upload_observation;

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
from app.customer_invoice
order by customer_id;

delete
from app.customer_invoice;
commit;

update app.customer
set enabled= true,
    activation_date=now();
commit;

update app.customer_product
set enabled= true,
    activation_date=now();
commit;

update app.customer
set under_observation= true;
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
