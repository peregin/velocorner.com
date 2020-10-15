create table location(
    location varchar primary key,
    latitude numeric(8, 4) not null, -- -90 -> 90
    longitude numeric(8, 4) not null -- -180 -> 80
);