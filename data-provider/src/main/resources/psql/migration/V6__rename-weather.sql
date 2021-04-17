alter table weather rename to forecast;

drop table sun;

create table weather(
    location varchar primary key,
    data jsonb not null
);

-- cleanup timestamps, will have different keys
delete from attribute;