create table activity(
    id bigint primary key,
    type varchar,
    athlete_id bigint not null,
    data jsonb not null
);

create index activity_type_idx on activity(type);
create index activity_athlete_id_idx on activity(athlete_id);