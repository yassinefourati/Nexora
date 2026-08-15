-- V12__real_schema_views.sql
--
-- Adds the 11 views + 1 materialized view present in the authoritative
-- pg_dump (db_admin-202608131126.sql) but missing from the Flyway history.
-- Every definition below is reproduced verbatim (column-for-column,
-- clause-for-clause) from the dump's CREATE VIEW / CREATE MATERIALIZED VIEW
-- statements -- see:
--   grep -n "CREATE VIEW public.<name>" / "CREATE MATERIALIZED VIEW public.<name>"
-- in db_admin-202608131126.sql.
--
-- These are DDL-only additions: no Java code is required to call them
-- directly (Hibernate never targets a view unless a read-only @Entity is
-- explicitly mapped to it -- see the accompanying read-only entities for
-- active_users_view, user_permissions_view, user_roles_view and
-- menu_hierarchy_view).

-- 1. active_users_view -- active, non-deleted users
CREATE VIEW public.active_users_view AS
 SELECT id,
    username,
    email,
    first_name,
    last_name,
    last_login_at,
    created_at
   FROM public.users
  WHERE (((status)::text = 'active'::text) AND (deleted_at IS NULL));

-- 2. audit_summary_view -- audit_logs grouped by entity_type/action/day
CREATE VIEW public.audit_summary_view AS
 SELECT entity_type,
    action,
    date_trunc('day'::text, created_at) AS day,
    count(*) AS event_count
   FROM public.audit_logs
  GROUP BY entity_type, action, (date_trunc('day'::text, created_at));

-- 3. login_activity_view -- login_history joined with users
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

-- 4. menu_hierarchy_view -- recursive CTE building the full menu tree
CREATE VIEW public.menu_hierarchy_view AS
 WITH RECURSIVE tree AS (
         SELECT mi.id,
            mi.menu_id,
            mi.parent_menu_item_id,
            mi.label,
            mi.route_path,
            mi.sort_order,
            mi.is_active,
            1 AS depth,
            (mi.label)::text AS path
           FROM public.menu_items mi
          WHERE ((mi.parent_menu_item_id IS NULL) AND (mi.deleted_at IS NULL))
        UNION ALL
         SELECT c.id,
            c.menu_id,
            c.parent_menu_item_id,
            c.label,
            c.route_path,
            c.sort_order,
            c.is_active,
            (t.depth + 1),
            ((t.path || ' > '::text) || (c.label)::text)
           FROM (public.menu_items c
             JOIN tree t ON ((c.parent_menu_item_id = t.id)))
          WHERE (c.deleted_at IS NULL)
        )
 SELECT id,
    menu_id,
    parent_menu_item_id,
    label,
    route_path,
    sort_order,
    is_active,
    depth,
    path
   FROM tree
  ORDER BY path;

-- 5. organization_hierarchy_view -- recursive CTE building the full org tree
CREATE VIEW public.organization_hierarchy_view AS
 WITH RECURSIVE org_tree AS (
         SELECT organizations.id,
            organizations.parent_organization_id,
            organizations.name,
            organizations.code,
            1 AS depth,
            (organizations.name)::text AS path
           FROM public.organizations
          WHERE ((organizations.parent_organization_id IS NULL) AND (organizations.deleted_at IS NULL))
        UNION ALL
         SELECT o.id,
            o.parent_organization_id,
            o.name,
            o.code,
            (t.depth + 1),
            ((t.path || ' > '::text) || (o.name)::text)
           FROM (public.organizations o
             JOIN org_tree t ON ((o.parent_organization_id = t.id)))
          WHERE (o.deleted_at IS NULL)
        )
 SELECT id,
    parent_organization_id,
    name,
    code,
    depth,
    path
   FROM org_tree
  ORDER BY path;

-- 6. role_menu_view -- roles joined with role_menus/menu_items
CREATE VIEW public.role_menu_view AS
 SELECT r.id AS role_id,
    r.name AS role_name,
    mi.id AS menu_item_id,
    mi.label,
    mi.route_path,
    rm.can_view
   FROM ((public.roles r
     JOIN public.role_menus rm ON ((rm.role_id = r.id)))
     JOIN public.menu_items mi ON ((mi.id = rm.menu_item_id)))
  WHERE ((r.deleted_at IS NULL) AND (mi.deleted_at IS NULL));

-- 7. role_permissions_view -- roles joined with role_permissions/permissions
CREATE VIEW public.role_permissions_view AS
 SELECT r.id AS role_id,
    r.name AS role_name,
    p.id AS permission_id,
    p.code AS permission_code,
    rp.conditions
   FROM ((public.roles r
     JOIN public.role_permissions rp ON ((rp.role_id = r.id)))
     JOIN public.permissions p ON ((p.id = rp.permission_id)))
  WHERE (r.deleted_at IS NULL);

-- 8. user_notifications_view -- user_notifications joined with notifications
CREATE VIEW public.user_notifications_view AS
 SELECT un.user_id,
    n.id AS notification_id,
    n.title,
    n.body,
    n.channel,
    un.is_read,
    un.read_at,
    un.delivered_at,
    n.created_at
   FROM (public.user_notifications un
     JOIN public.notifications n ON ((n.id = un.notification_id)))
  ORDER BY n.created_at DESC;

-- 9. user_permissions_view -- effective permissions via user_roles/role_permissions/permissions
CREATE VIEW public.user_permissions_view AS
 SELECT DISTINCT u.id AS user_id,
    u.username,
    p.resource,
    p.action,
    p.code AS permission_code,
    ur.organization_id
   FROM (((public.users u
     JOIN public.user_roles ur ON (((ur.user_id = u.id) AND ((ur.expires_at IS NULL) OR (ur.expires_at > now())))))
     JOIN public.role_permissions rp ON ((rp.role_id = ur.role_id)))
     JOIN public.permissions p ON ((p.id = rp.permission_id)))
  WHERE (u.deleted_at IS NULL);

-- 10. user_permissions_mv -- materialized version of user_permissions_view,
-- built WITH NO DATA so it must be populated by an explicit initial
-- REFRESH MATERIALIZED VIEW before it is queried. The unique index below
-- enables REFRESH MATERIALIZED VIEW CONCURRENTLY (used by the
-- refresh_user_permissions_mv() function -- see V13).
CREATE MATERIALIZED VIEW public.user_permissions_mv AS
 SELECT user_id,
    username,
    resource,
    action,
    permission_code,
    organization_id
   FROM public.user_permissions_view
  WITH NO DATA;

CREATE UNIQUE INDEX uq_user_permissions_mv ON public.user_permissions_mv USING btree (user_id, permission_code, COALESCE(organization_id, '00000000-0000-0000-0000-000000000000'::uuid));

-- 11. user_roles_view -- users joined with user_roles/roles/organizations
CREATE VIEW public.user_roles_view AS
 SELECT u.id AS user_id,
    u.username,
    r.id AS role_id,
    r.name AS role_name,
    ur.organization_id,
    o.name AS organization_name,
    ur.expires_at
   FROM (((public.users u
     JOIN public.user_roles ur ON ((ur.user_id = u.id)))
     JOIN public.roles r ON ((r.id = ur.role_id)))
     LEFT JOIN public.organizations o ON ((o.id = ur.organization_id)))
  WHERE ((u.deleted_at IS NULL) AND (r.deleted_at IS NULL));
