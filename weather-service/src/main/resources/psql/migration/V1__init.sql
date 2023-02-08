create table forecast(
    location varchar not null,
    update_time timestamptz not null default now(),
    data jsonb not null,
    primary key(location, update_time)
);

create table weather(
    location varchar not null,
    data jsonb not null,
    primary key(location)
);

comment on table weather is 'stores the current weather conditions';
comment on table forecast is 'stores the future - forecasted - weather conditions';

