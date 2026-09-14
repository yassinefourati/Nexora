-- Phase 4 of the backend plan (ADR 0001 closure, ADR 0004 domain hardening).
--
-- 1. users.status is an enum in the code (UserStatus); tighten the check
--    constraint to the values the enum knows. 'deleted' was never written by
--    the application (soft delete uses deleted_at); fold any such row into
--    'inactive' so the constraint can be applied.
-- 2. Keycloak is the only authentication authority. The local password and
--    lockout columns are dead since V15 made password_hash nullable; drop them,
--    together with the PL/pgSQL helpers from V13 that read or wrote them.

UPDATE users SET status = 'inactive' WHERE status = 'deleted';

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_status_check;
ALTER TABLE users
    ADD CONSTRAINT users_status_check
    CHECK (status IN ('active', 'inactive', 'suspended', 'pending'));

DROP FUNCTION IF EXISTS public.create_user;
DROP FUNCTION IF EXISTS public.handle_failed_login;
DROP FUNCTION IF EXISTS public.handle_successful_login;
DROP FUNCTION IF EXISTS public.check_permission;

ALTER TABLE users
    DROP COLUMN IF EXISTS password_hash,
    DROP COLUMN IF EXISTS password_changed_at,
    DROP COLUMN IF EXISTS failed_login_attempts,
    DROP COLUMN IF EXISTS locked_until;
