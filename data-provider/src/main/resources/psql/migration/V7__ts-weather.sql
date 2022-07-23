delete from forecast;
alter table forecast drop column update_time;
alter table forecast add column update_time timestamptz not null default now();
alter table forecast add primary key (location, update_time);