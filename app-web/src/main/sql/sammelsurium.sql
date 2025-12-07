select *
from app.user_account;

select *
from app.consumer;

select *
from app.consumer_upload;

select *
from app.consumer_billing;

update app.consumer
set enabled= true;
commit;

call app.clearAccounts();

update app.consumer_upload
set status= 'done';
commit;



