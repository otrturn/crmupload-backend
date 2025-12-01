select *
from app.user_account;

delete from app.user_account;
insert into app.user_account (id, username, password, roles, lastlogin)
values (1,'ralf','{bcrypt}$2a$10$2KpcUEUrGLOwlfWVa4ojwu7DsRE/iBZHfsqucPWARrACuS.o52ihq','ROLE_USER',now());
commit;

