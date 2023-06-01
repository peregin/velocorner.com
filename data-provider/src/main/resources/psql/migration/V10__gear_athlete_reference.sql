-- references account(athlete_id) and activity(athlete_id)
alter table gear add column athlete_id bigint;

-- fill it with values when possible
update gear g set athlete_id = (select a.athlete_id from activity a where (a.data->>'gear_id') like g.id limit 1) where g.athlete_id is null;