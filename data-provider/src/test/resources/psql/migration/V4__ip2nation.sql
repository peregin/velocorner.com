create table ip2nation (
  ip numeric(11) not null default '0',
  country varchar(2) not null default '',
  primary key (ip)
);

-- THIS IS TEST MODE, has less data to speed integration tests
INSERT INTO ip2nation (ip, country) VALUES(687865856, 'za');
INSERT INTO ip2nation (ip, country) VALUES(3164340224, 'hu');
INSERT INTO ip2nation (ip, country) VALUES(1080957952, 'ch');
INSERT INTO ip2nation (ip, country) VALUES(1080967168, 'ch');
INSERT INTO ip2nation (ip, country) VALUES(1080987648, 'ch');
INSERT INTO ip2nation (ip, country) VALUES(1080989952, 'ch');


