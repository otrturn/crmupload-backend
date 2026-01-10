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
from app.customer_blocked;

select *
from app.customer_activation;

SELECT token
from app.customer_activation
WHERE customer_id = (SELECT customer_id from app.customer WHERE email_address = 'ralf+00@test.de');

select *
from app.customer_acknowledgement;

select upload_id,
       customer_id,
       source_system,
       crm_system,
       crm_url,
       last_error,
       statistics,
       status,
       is_test
from app.crm_upload
order by upload_id desc;

select upload_id, customer_id, source_system, crm_system, status, is_test
from app.crm_upload_observation;

select duplicate_check_id, customer_id, source_system, statistics, status
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

update app.customer_invoice
set invoice_mailing_date=NULL;
commit;

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

SELECT *
FROM app.kpi_open_products_revenue();

update app.crm_upload
set is_test= true
where crm_url like '%demo%';
commit;

select (select count(*) from app.customer)                                           as customer,
       (select count(*) from app.customer where enabled = true)                      as customerEnabled,
       (select count(*) from app.customer_product where product = 'crm-upload')      as productCrmUpload,
       (select count(*) from app.customer_product where product = 'duplicate-check') as productDuplicateCheck;

