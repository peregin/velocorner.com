create table gear(
    id varchar primary key,
    type varchar,
    data jsonb not null
);

create index gear_type_idx on gear(type);