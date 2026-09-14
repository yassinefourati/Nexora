-- Phase 5 of the backend plan (ADR 0006): the effective permission set is
-- resolved at authentication from user_roles -> role_permissions -> permissions
-- and cached in the shared store, invalidated on every access change. The
-- materialized view that used to be refreshed every five minutes for nobody
-- has no reader left.
DROP MATERIALIZED VIEW IF EXISTS public.user_permissions_mv;
DROP FUNCTION IF EXISTS public.refresh_user_permissions_mv();
