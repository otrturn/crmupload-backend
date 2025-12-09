INSERT INTO app.page_visits (page_id, visited)
VALUES ('home', '2025-12-08 21:08:16.168471+00'),
       ('leistungs-beschreibung', '2025-12-08 21:08:18.225172+00'),
       ('home', '2025-12-08 21:08:20.329846+00'),
       ('leistungs-beschreibung', '2025-12-08 21:08:30.506592+00'),
       ('home', '2025-12-08 21:08:31.591214+00'),
       ('home', '2025-12-09 00:30:11.684624+00'),
       ('home', '2025-12-09 03:51:46.892965+00'),
       ('home', '2025-12-09 12:11:33.950873+00'),
       ('home', '2025-12-09 13:26:09.713688+00'),
       ('home', '2025-12-09 13:26:30.285478+00'),
       ('home', '2025-12-09 14:21:15.569091+00');
commit;

select *
from app.page_visits
order by visited desc;

select 'INSERT INTO app.page_visits (page_id, visited) VALUES'
union all
select '(''' || '' || '' || page_id || ''',''' || visited || '''),'
from app.page_visits;
