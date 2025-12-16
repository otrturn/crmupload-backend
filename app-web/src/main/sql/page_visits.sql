select *
from app.page_visits
order by visited desc;

select 'INSERT INTO app.page_visits (page_id, visited) VALUES'
union all
select '(''' || '' || '' || page_id || ''',''' || visited || '''),'
from app.page_visits;
