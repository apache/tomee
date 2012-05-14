create table users (user_name varchar(15) not null primary key, user_pass varchar(15) not null);
create table user_roles (user_name varchar(15) not null, role_name varchar(15) not null, primary key (user_name, role_name));
insert into users(user_name, user_pass) values('tomee', 'tomee');
insert into user_roles(user_name, role_name) values('tomee', 'admin');
