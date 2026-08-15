-- The application only ever stores and displays IP address strings — it never
-- performs inet-specific SQL operations (subnet containment, range queries,
-- etc.). Postgres does not implicitly cast varchar -> inet the way it does
-- for citext, so JPA/Hibernate can't bind a plain String parameter against
-- an inet column without extra driver-specific glue code (confirmed live:
-- creating a User failed with "column is of type inet but expression is of
-- type character varying/bytea" depending on the JDBC binding attempted).
-- Postgres does allow the reverse cast (inet -> varchar) automatically, so
-- this is a safe, one-directional simplification.
ALTER TABLE audit_logs ALTER COLUMN ip_address TYPE VARCHAR(45);
ALTER TABLE auth_logs ALTER COLUMN ip_address TYPE VARCHAR(45);
ALTER TABLE sessions ALTER COLUMN ip_address TYPE VARCHAR(45);

-- login_history.ip_address is depended on by login_activity_view's rule (V12) —
-- drop and recreate the view around the alter, identical definition otherwise.
DROP VIEW public.login_activity_view;

ALTER TABLE login_history ALTER COLUMN ip_address TYPE VARCHAR(45);

CREATE VIEW public.login_activity_view AS
 SELECT u.id AS user_id,
    u.username,
    lh.login_at,
    lh.logout_at,
    lh.success,
    lh.ip_address
   FROM (public.login_history lh
     JOIN public.users u ON ((u.id = lh.user_id)))
  ORDER BY lh.login_at DESC;
