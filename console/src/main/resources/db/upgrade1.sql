alter table "job" add "user" varchar(255), add "created" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
alter table "job_run" add "created" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
alter table "log" 
	alter "timestamp" type TIMESTAMP WITH TIME ZONE,
	alter "timestamp" set DEFAULT NOW(),
	alter "timestamp" set NOT NULL;
alter table "users" 
	add "timestamp" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
	add "email" varchar(255);

ALTER TABLE job ADD CONSTRAINT user_fk FOREIGN KEY ("user") REFERENCES "users"(username) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE job_run ADD COLUMN "activity" VARCHAR(255);

update "job" set "user" = 'admin';
update "users" set "email" = 'no-reply@localhost';

alter table "job" alter "user" set not null;
alter table "users" alter "email" set not null;
alter table "job_run" add "lastSuccess" TIMESTAMP WITH TIME ZONE;
