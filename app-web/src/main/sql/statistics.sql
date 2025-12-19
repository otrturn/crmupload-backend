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

SELECT * FROM app.kpi_open_products_revenue();