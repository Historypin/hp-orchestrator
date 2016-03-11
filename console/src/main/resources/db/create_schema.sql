-- CLEAR SCHEMA --
DROP SEQUENCE IF EXISTS "seq_job" CASCADE;
DROP SEQUENCE IF EXISTS "seq_param" CASCADE;
DROP SEQUENCE IF EXISTS "seq_process" CASCADE;
DROP SEQUENCE IF EXISTS "seq_read_only_param" CASCADE;
DROP SEQUENCE IF EXISTS "seq_log" CASCADE;
DROP TABLE IF EXISTS "job" CASCADE;
DROP TABLE IF EXISTS "param" CASCADE;
DROP TABLE IF EXISTS "process" CASCADE;
DROP TABLE IF EXISTS "read_only_param" CASCADE;
DROP TABLE IF EXISTS "log" CASCADE;
DROP TABLE IF EXISTS "users" CASCADE;
DROP TABLE IF EXISTS "authorities" CASCADE;

-- JOB TABLE BEGIN --
CREATE SEQUENCE "seq_job" START 1 INCREMENT BY 50;

CREATE TABLE "job" (
  "id"        INT8 PRIMARY KEY DEFAULT nextval('seq_job') NOT NULL,
  "name"      VARCHAR(255),
  "source"    VARCHAR(255),
  "target"    VARCHAR(255),
  "status"    VARCHAR(255)
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

-- PROCESS TABLE BEGIN --
CREATE SEQUENCE "seq_process" START 1 INCREMENT BY 50;

CREATE TABLE "process" (
  "id"        INT8 PRIMARY KEY DEFAULT nextval('seq_process') NOT NULL,
  "job_id"    INT8 REFERENCES job(id) ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE INDEX "ix_process_job_id" ON "process" ("job_id");
-- PROCESS TABLE END --

-- READ_ONLY_PARAM TABLE BEGIN --
CREATE SEQUENCE "seq_read_only_param" START 1 INCREMENT BY 50;

CREATE TABLE "read_only_param" (
  "id"          INT8 PRIMARY KEY DEFAULT nextval('seq_read_only_param') NOT NULL,
  "key"         VARCHAR(255),
  "value"       VARCHAR(1024),
  "process_id"  INT8 REFERENCES process(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX "ix_read_only_param_process_id" ON "read_only_param" ("process_id");

CREATE UNIQUE INDEX ix_read_only_param_process_id_key on read_only_param (process_id, key);

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
  "task_type"   VARCHAR(255),
  "status"      VARCHAR(255),
  "message"     VARCHAR(1024),
  "process_id"  INT8 REFERENCES process(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX "ix_log_process_id" ON "log" ("process_id");
--  LOG TABLE END --

-- USERS TABLE BEGIN --
CREATE TABLE "users" (
  "username"    VARCHAR(255) PRIMARY KEY NOT NULL,
  "password"    VARCHAR(255) NOT NULL,
  "enabled"     BOOLEAN NOT NULL
);
-- USERS TABLE END --

-- AUTHORITIES TABLE BEGIN --
CREATE TABLE "authorities" (
  "username"    VARCHAR(255) NOT NULL REFERENCES users(username) ON UPDATE CASCADE ON DELETE CASCADE,
  "authority"   VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX ix_authorities_username on authorities (username, authority);
-- AUTHORITIES TABLE END --
