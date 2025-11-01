--liquibase formatted sql

--changeset author:admin id:001-create-telegram-user-table

create table if not exists telegram_user (
    id bigint primary key,
    first_name varchar(255) not null,
    last_name varchar(255),
    username varchar(255) not null,
    auth_date varchar(255) not null
);

--rollback DROP TABLE telegram_user;