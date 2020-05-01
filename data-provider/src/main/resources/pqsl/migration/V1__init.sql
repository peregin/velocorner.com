create table activity {
    id bigint primary key,
    type varchar,
    athlete_id bigint not null
};

create activity_type_idx on activity(type);
create activity_athlete_id_idx on activity(athlete_id);