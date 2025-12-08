select * from app.page_visits;

delete from app.page_visits;
commit;

select *
from app.user_account;

select *
from app.customer;

select *
from app.crm_upload;

select *
from app.customer_billing
order by customer_id;

update app.customer
set enabled= true;
commit;

call app.clearAccounts();

update app.crm_upload
set status= 'done';
commit;

select *
from app.export_billing();

SELECT
    cu.created         AS ts,
    cu.source_system   AS source_system,
    cu.crm_system      AS crm_system,
    cu.crm_customer_id AS crm_customer_id,
    cu.status          AS status
FROM app.crm_upload cu
         JOIN app.customer c
              ON c.customer_id = cu.customer_id
WHERE c.email_address = 'ralf+0@test.de'
ORDER BY cu.created DESC
