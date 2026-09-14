-- 1. Optimistic locking -------------------------------------------------------
-- Every mutable table gets a version counter mapped by @Version on the JPA base
-- classes. Hibernate increments it on each update and refuses to write a row
-- whose version changed since it was read (surfaced as HTTP 409
-- CONCURRENT_MODIFICATION). DEFAULT 0 keeps existing rows valid.
DO $$
DECLARE
    t text;
BEGIN
    FOREACH t IN ARRAY ARRAY[
        'users','roles','permissions','role_permissions','user_roles',
        'organizations','departments','teams','organization_members',
        'app_modules','feature_flags','settings',
        'menus','menu_items','role_menus','menu_permissions',
        'api_keys','audit_logs','auth_logs','login_history','rate_limits','sessions','user_identity_providers',
        'error_logs','system_events',
        'notification_templates','notifications','user_notifications',
        'tags','entity_tags','comments','attachments','metadata_kv'
    ] LOOP
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0', t);
    END LOOP;
END $$;

-- 2. One audit path ------------------------------------------------------------
-- V13 installed fn_audit_trigger() on six tables. The application never sets the
-- app.current_user_id session variable the trigger reads, so every trigger row
-- carried user_id = NULL, and the same write was also recorded (with a real
-- actor) by AuditAspect/AuditLogEventListener. Two rows per write, one of them
-- anonymous. The application-level path is kept; the triggers are dropped. The
-- function itself stays so it can be re-attached if the actor is ever wired in.
DROP TRIGGER IF EXISTS trg_audit_permissions      ON public.permissions;
DROP TRIGGER IF EXISTS trg_audit_role_permissions ON public.role_permissions;
DROP TRIGGER IF EXISTS trg_audit_roles            ON public.roles;
DROP TRIGGER IF EXISTS trg_audit_settings         ON public.settings;
DROP TRIGGER IF EXISTS trg_audit_user_roles       ON public.user_roles;
DROP TRIGGER IF EXISTS trg_audit_users            ON public.users;
