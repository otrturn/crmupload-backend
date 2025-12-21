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
from app.customer_acknowledgement;

select upload_id, customer_id, source_system, crm_system, statistics, status
from app.crm_upload
order by upload_id desc;

select upload_id, customer_id, source_system, crm_system, status
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