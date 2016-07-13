-- CLEAR SCHEMA --
DROP SEQUENCE IF EXISTS "seq_job" CASCADE;
DROP SEQUENCE IF EXISTS "seq_param" CASCADE;
DROP SEQUENCE IF EXISTS "seq_job_run" CASCADE;
DROP SEQUENCE IF EXISTS "seq_read_only_param" CASCADE;
DROP SEQUENCE IF EXISTS "seq_log" CASCADE;
DROP TABLE IF EXISTS "job" CASCADE;
DROP TABLE IF EXISTS "param" CASCADE;
DROP TABLE IF EXISTS "job_run" CASCADE;
DROP TABLE IF EXISTS "read_only_param" CASCADE;
DROP TABLE IF EXISTS "log" CASCADE;
DROP TABLE IF EXISTS "users" CASCADE;
DROP TABLE IF EXISTS "authorities" CASCADE;
DROP INDEX IF EXISTS "ix_authority_username" CASCADE;
DROP FUNCTION IF EXISTS update_read_only_param_error();

-- JOB TABLE BEGIN --
CREATE SEQUENCE "seq_job" START 1 INCREMENT BY 50;

CREATE TABLE "job" (
  "id"              INT8 PRIMARY KEY DEFAULT nextval('seq_job') NOT NULL,
  "name"            VARCHAR(255),
  "source"          VARCHAR(255),
  "target"          VARCHAR(255),
  "last_job_run_id" INT8 REFERENCES job_run(id),
  "user"            VARCHAR(255) REFERENCES users(username) NOT NULL,
  "created"         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
-- JOB TABLE END --

-- PARAM TABLE BEGIN --
CREATE SEQUENCE "seq_param" START 1 INCREMENT BY 50;

CREATE TABLE "param" (
  "id"        INT8 PRIMARY KEY DEFAULT nextval('seq_param') NOT NULL,
  "key"       VARCHAR(255),
  "value"     VARCHAR(1024),
  "job_id"    INT8 REFERENCES job(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX "ix_param_job_id" ON "param" ("job_id");

CREATE UNIQUE INDEX ix_param_job_id_key on param (job_id, key);
-- PARAM TABLE END --

 ALTER TABLE ONLY job_run
     ADD CONSTRAINT fkds874o50ch763h968aedawv77 FOREIGN KEY (parentrun_id) REFERENCES job_run(id);

-- JOB_RUN TABLE BEGIN --
CREATE SEQUENCE "seq_job_run" START 1 INCREMENT BY 50;

CREATE TABLE "job_run" (
  "id"        INT8 PRIMARY KEY DEFAULT nextval('seq_job_run') NOT NULL,
  "status"    VARCHAR(255),
  "result"    VARCHAR(255),
  "activity"  VARCHAR(255),
  "job_id"    INT8 REFERENCES job(id) ON UPDATE CASCADE ON DELETE CASCADE,
  "created"   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  "dtype"     VARCHAR(31),
  "parentrun_id" INT8 REFERENCES job_run(id) ON UPDATE CASCADE ON DELETE CASCADE,
  "last_started"  TIMESTAMP WITH TIME ZONE
);
CREATE INDEX "ix_job_run_job_id" ON "job_run" ("job_id");
-- JOB_RUN TABLE END --

-- READ_ONLY_PARAM TABLE BEGIN --
CREATE SEQUENCE "seq_read_only_param" START 1 INCREMENT BY 50;

CREATE TABLE "read_only_param" (
  "id"          INT8 PRIMARY KEY DEFAULT nextval('seq_read_only_param') NOT NULL,
  "key"         VARCHAR(255),
  "value"       VARCHAR(1024),
  "job_run_id"  INT8 REFERENCES job_run(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX "ix_read_only_param_job_run_id" ON "read_only_param" ("job_run_id");

CREATE UNIQUE INDEX ix_read_only_param_job_run_id_key on read_only_param (job_run_id, key);

CREATE FUNCTION update_read_only_param_error() RETURNS TRIGGER AS $update_read_only_param_error$
BEGIN
  RAISE EXCEPTION 'Table % is read-only. UPDATE operation is forbidden.', TG_TABLE_NAME;
END;
$update_read_only_param_error$ LANGUAGE plpgsql;

CREATE TRIGGER update_read_only_param_error BEFORE UPDATE ON read_only_param
FOR EACH ROW EXECUTE PROCEDURE update_read_only_param_error();
--  READ_ONLY_PARAM TABLE END --

-- LOG TABLE BEGIN --
CREATE SEQUENCE "seq_log" START 1 INCREMENT BY 50;

CREATE TABLE "log" (
  "id"          INT8 PRIMARY KEY DEFAULT nextval('seq_log') NOT NULL,
  "timestamp"   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  "level"       VARCHAR(255),
  "message"     VARCHAR,
  "job_run_id"  INT8 REFERENCES job_run(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX "ix_log_job_run_id" ON "log" ("job_run_id");
--  LOG TABLE END --

-- USERS TABLE BEGIN --
CREATE TABLE "users" (
  "username"    VARCHAR(255) PRIMARY KEY NOT NULL,
  "email"       VARCHAR(255) NOT NULL,
  "password"    VARCHAR(255) NOT NULL,
  "enabled"     BOOLEAN NOT NULL
);
-- USERS TABLE END --

-- AUTHORITIES TABLE BEGIN --
CREATE TABLE "authorities" (
  "username"     VARCHAR(255) NOT NULL REFERENCES "users"(username) ON UPDATE CASCADE ON DELETE CASCADE,
  "authority"   VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX ix_authority_username on "authorities"(username, authority);
-- AUTHORITIES TABLE END --

ALTER TABLE job ADD CONSTRAINT last_job_run_fk FOREIGN KEY ("last_job_run_id") REFERENCES job_run(id) ON DELETE SET NULL;
ALTER TABLE job ADD CONSTRAINT user_fk FOREIGN KEY ("user") REFERENCES "users"(username) ON UPDATE CASCADE ON DELETE CASCADE;
