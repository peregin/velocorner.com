create table if not exists athlete_performance_analysis (
  id bigserial primary key,
  athlete_id bigint not null,
  fingerprint varchar(64) not null,
  based_on varchar(256) not null,
  summary text not null,
  created_at timestamp not null default now()
);

create index if not exists idx_athlete_performance_analysis_athlete_generated
  on athlete_performance_analysis (athlete_id, created_at desc);

create unique index if not exists uq_athlete_performance_analysis_athlete_fingerprint
  on athlete_performance_analysis (athlete_id, fingerprint);
