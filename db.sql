-- auto-generated definition
create table role_table
(
    id   serial      not null
        constraint role_table_pk
            primary key,
    name varchar(20) not null
);


-- auto-generated definition
create table user_table
(
    id       serial not null
        constraint user_table_pk
            primary key,
    login    varchar(50),
    password varchar(500),
    role_id  integer
        constraint user_table_role_table_id_fk
            references role_table
);


create unique index user_table_login_uindex
    on user_table (login);

insert into role_table(name)
values ('ROLE_ADMIN');
insert into role_table(name)
values ('ROLE_USER');

create table game_card
(
    id    serial       not null
        primary key,
    name  varchar(100) not null,
    image bytea        not null
);
create table maps
(
    id    serial       not null
        primary key,
    name  varchar(100) not null,
    image bytea        not null
);



create or replace function bytea_import(p_path text, p_result out bytea)
    language plpgsql as
$$
declare
    l_oid oid;
begin
    select lo_import(p_path) into l_oid;
    select lo_get(l_oid) INTO p_result;
    perform lo_unlink(l_oid);
end;
$$;

insert into game_card(name, image)
VALUES ('test', bytea_import('glyphicons-halflings.png'));