-- V6__observability.sql
-- Tables: error_logs, system_events

-- set_updated_at() is defined here (its first point of use) rather than in the later
-- functions/triggers migration (V13), since Flyway applies migrations in version order
-- and V6/V7 both need it before V13 runs. V13 does NOT redefine it, and does not
-- re-create the triggers on error_logs/system_events/notification_templates/
-- notifications/user_notifications that V6/V7 already create using it.
CREATE FUNCTION set_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$;

CREATE TABLE error_logs (
    id            UUID          NOT NULL DEFAULT gen_random_uuid(),
    source        VARCHAR(150),
    error_message TEXT          NOT NULL,
    stack_trace   TEXT,
    context       JSONB         NOT NULL DEFAULT '{}'::jsonb,
    severity      VARCHAR(20)   NOT NULL DEFAULT 'error',
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT error_logs_pkey PRIMARY KEY (id),
    CONSTRAINT error_logs_severity_check CHECK (severity IN ('warning', 'error', 'critical'))
);

CREATE INDEX idx_error_logs_severity ON error_logs (severity);

CREATE TRIGGER trg_set_updated_at_error_logs
    BEFORE UPDATE ON error_logs
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE system_events (
    id           UUID          NOT NULL DEFAULT gen_random_uuid(),
    event_type   VARCHAR(100)  NOT NULL,
    severity     VARCHAR(20)   NOT NULL DEFAULT 'info',
    source       VARCHAR(100),
    payload      JSONB         NOT NULL DEFAULT '{}'::jsonb,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT system_events_pkey PRIMARY KEY (id),
    CONSTRAINT system_events_severity_check CHECK (severity IN ('debug', 'info', 'warning', 'critical'))
);

CREATE INDEX idx_system_events_type ON system_events (event_type);

CREATE TRIGGER trg_set_updated_at_system_events
    BEFORE UPDATE ON system_events
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
