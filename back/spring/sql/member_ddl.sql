drop table if exists member CASCADE;
create table member
(
    id bigint generated by default as identity,
    user_id varchar(15) not null ,
    user_pw varchar(100) not null ,
    email varchar(30) not null,
    salt varchar(40),

    primary key (id),
    unique (user_id)
);