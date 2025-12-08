select *
from app.user_account;

select *
from app.customer;

select *
from app.customer_upload;

select *
from app.customer_billing;

update app.customer
set enabled= true;
commit;

call app.clearAccounts();

update app.customer_upload
set status= 'done';
commit;

select *
from app.export_billing();

