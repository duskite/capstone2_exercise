drop table if exists member CASCADE;
create table member
(
    id bigint generated by default as identity,
    user_id varchar(15) not null ,
    user_pw varchar(100) not null ,
    email varchar(30) not null,
    salt varchar(40),

    nickname varchar(20),
    introduce text,
    region text,
    cell_phone varchar(20),

    fav_soccer boolean,
    fav_futsal boolean,
    fav_baseball boolean,
    fav_basketball boolean,
    fav_badminton boolean,
    fav_cycle boolean,

    primary key (id)
);