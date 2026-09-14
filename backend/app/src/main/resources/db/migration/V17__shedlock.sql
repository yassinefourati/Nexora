-- ShedLock coordination table (ADR 0008): every @Scheduled job takes a row-level
-- lock here so exactly one replica runs it per interval. Schema as required by
-- shedlock-provider-jdbc-template.
CREATE TABLE IF NOT EXISTS shedlock (
    name        VARCHAR(64)  NOT NULL,
    lock_until  TIMESTAMP    NOT NULL,
    locked_at   TIMESTAMP    NOT NULL,
    locked_by   VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

COMMENT ON TABLE shedlock IS 'ShedLock scheduler locks; one row per job name, owned by the replica named in locked_by until lock_until.';
