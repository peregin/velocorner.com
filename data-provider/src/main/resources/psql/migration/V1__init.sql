create table activity(
    id bigint primary key,
    type varchar,
    athlete_id bigint not null,
    data jsonb not null
);

create index activity_type_idx on activity(type);
create index activity_athlete_id_idx on activity(athlete_id);

create table account(
    athlete_id bigint primary key,
    data jsonb not null
);

create table weather(
    location varchar,
    update_time bigint,
    data jsonb not null,
    primary key(location, update_time)
);

create table sun(
    location varchar,
    update_date varchar,
    data jsonb not null,
    primary key(location, update_date)
);

