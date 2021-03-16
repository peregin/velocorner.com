
show max_connections;

select data->>'displayName' from account;

select jsonb_pretty(data) from account;

select activity.id, max(cast(data->>'total_elevation_gain' as numeric)) metric from activity
where athlete_id = 432909 and type = 'Ride' and data->>'total_elevation_gain' is not null
group by activity.id
order by metric desc
limit 1;

select max(cast(data->>'total_elevation_gain' as numeric)) metric from activity
where athlete_id = 432909 and type = 'Ride' and data->>'total_elevation_gain' is not null;


select cast(data->>'total_elevation_gain' as numeric) metric, data 
from activity 
where athlete_id = 432909 and type = 'Ride' and cast(data->>'total_elevation_gain' as numeric) is not null 
order by cast(data->>'total_elevation_gain' as numeric) desc limit 1

select jsonb_pretty(data) from account where data->>'displayName' like '%Levente%';


update account SET data = jsonb_set(data, '{role}', '"admin"'::jsonb) where athlete_id = 432909;
select data->>'role' from account where athlete_id = 432909;

UPDATE account SET data = jsonb_set(data, '{lastUpdate}', '"2020-07-10T21:02:51Z"'::jsonb) where athlete_id = 432909;
select data->>'lastUpdate' from account where data->>'displayName' like '%Levente%';


select now();

select * from gear;

select data->>'start_date' from activity where athlete_id = 432909 limit 10;


select data from activity
where athlete_id = 432909 and (data->>'start_date')::timestamp with time zone > now()
order by data->>'start_date' desc

select distinct extract(year from (data->>'start_date')::timestamp) as years from activity where athlete_id = 432909 order by years desc;

-- ip 2 nation
select country from ip2nation where ip < (('85.1.45.31'::inet - '0.0.0.0'::inet)::numeric) order by ip desc limit 1;

select * from ip2nation where ip < (('188.156.14.255'::inet - '0.0.0.0'::inet)::numeric) order by ip desc limit 1;

select * from ip2nation where country = 'HU';


select athlete_id::text, (data->>'start_date')::timestamp, 
  ((data->>'distance')::numeric/1000)::numeric(10, 2), 
  jsonb_pretty(data) from activity 
where athlete_id = 432909 order by data->>'start_date' desc;



