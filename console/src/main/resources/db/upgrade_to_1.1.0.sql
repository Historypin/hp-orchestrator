-- param table
alter table "param" alter "key" set NOT NULL;
alter table "param" add "type" VARCHAR(255);
alter table "param" rename "value" to "string_value";
alter table "param" add "blob_data" BYTEA;
alter table "param" add "blob_name" VARCHAR(255);
alter table "param" alter "job_id" set NOT NULL;

update "param" set "type" = 'stringParam' where "type" is NULL;

alter table "param" alter "type" set NOT NULL;

-- job_run table
alter table "job_run" add "dtype" VARCHAR(31);
alter table "job_run" add "parentrun_id" INT8 REFERENCES job_run(id) on UPDATE CASCADE ON DELETE CASCADE;
alter table "job_run" add "lastrun_id" INT8 REFERENCES job_run(id) on UPDATE CASCADE ON DELETE CASCADE;

update "job_run" set "dtype" = 'JobRun' where "dtype" is NULL;
alter table "job_run" alter "dtype" set NOT NULL;

-- read_only_param
drop trigger update_read_only_param_error ON read_only_param;
alter table "read_only_param" alter "key" set not NULL;
alter table "read_only_param" rename "value" to "string_value";
update "read_only_param" set "string_value" = 'StringReadOnlyParam' where "string_value" is NULL;
alter table "read_only_param" alter "string_value" set not NULL;
alter table "read_only_param" add "blob_data" BYTEA;
alter table "read_only_param" add "blob_name" VARCHAR(255);

--$update_read_only_param_error$ LANGUAGE plpgsql;
CREATE TRIGGER update_read_only_param_error BEFORE UPDATE ON read_only_param
FOR EACH ROW EXECUTE PROCEDURE update_read_only_param_error();

-- CONSTRAINTS
ALTER TABLE job_run ADD CONSTRAINT parent_run_fk FOREIGN KEY ("parentrun_id") REFERENCES job_run(id) ON UPDATE CASCADE ON DELETE CASCADE;
