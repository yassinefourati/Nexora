-- V13__real_schema_functions_triggers.sql
--
-- Adds the 13 stored PL/pgSQL functions (+ refresh_user_permissions_mv, which
-- pairs with V12's user_permissions_mv) and the ~30 triggers present in the
-- authoritative pg_dump (db_admin-202608131126.sql) but missing from the
-- Flyway history. Every function body below is reproduced verbatim from the
-- dump.
--
-- DDL-ONLY: these exist for schema parity with the real/production database.
-- The Java service layer does NOT call any of these functions and continues
-- to implement its own independent business logic (permission checks, login
-- lockout, audit logging via AuditAspect/AuditEvent/AuditLogEventListener,
-- etc.) exactly as it currently does. No service/repository code in this
-- project issues native queries against these functions.

-- ============================================================================
-- FUNCTIONS
-- ============================================================================

CREATE FUNCTION public.assign_role(p_user_id uuid, p_role_id uuid, p_organization_id uuid DEFAULT NULL::uuid, p_expires_at timestamp with time zone DEFAULT NULL::timestamp with time zone) RETURNS uuid
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id UUID;
BEGIN
    IF p_organization_id IS NULL THEN
        INSERT INTO user_roles (user_id, role_id, organization_id, expires_at)
        VALUES (p_user_id, p_role_id, NULL, p_expires_at)
        ON CONFLICT (user_id, role_id) WHERE organization_id IS NULL
        DO UPDATE SET expires_at = EXCLUDED.expires_at
        RETURNING id INTO v_id;
    ELSE
        INSERT INTO user_roles (user_id, role_id, organization_id, expires_at)
        VALUES (p_user_id, p_role_id, p_organization_id, p_expires_at)
        ON CONFLICT (user_id, role_id, organization_id) WHERE organization_id IS NOT NULL
        DO UPDATE SET expires_at = EXCLUDED.expires_at
        RETURNING id INTO v_id;
    END IF;

    RETURN v_id;
END;
$$;

CREATE FUNCTION public.check_permission(p_user_id uuid, p_resource character varying, p_action character varying, p_organization_id uuid DEFAULT NULL::uuid) RETURNS boolean
    LANGUAGE plpgsql STABLE
    AS $$
DECLARE
    v_is_super BOOLEAN;
    v_status   VARCHAR;
    v_locked_until TIMESTAMPTZ;
    v_granted  BOOLEAN;
BEGIN
    SELECT is_superuser, status, locked_until
      INTO v_is_super, v_status, v_locked_until
    FROM users WHERE id = p_user_id AND deleted_at IS NULL;

    IF v_status IS NULL OR v_status <> 'active' THEN
        RETURN false;
    END IF;
    IF v_locked_until IS NOT NULL AND v_locked_until > now() THEN
        RETURN false;
    END IF;
    IF v_is_super THEN
        RETURN true;
    END IF;

    SELECT EXISTS (
        SELECT 1
        FROM user_roles ur
        JOIN roles r ON r.id = ur.role_id AND r.deleted_at IS NULL
        JOIN role_permissions rp ON rp.role_id = ur.role_id
        JOIN permissions p ON p.id = rp.permission_id
        WHERE ur.user_id = p_user_id
          AND p.resource = p_resource
          AND p.action = p_action
          AND (ur.expires_at IS NULL OR ur.expires_at > now())
          AND (
                ur.organization_id IS NULL
             OR ur.organization_id = p_organization_id
          )
    ) INTO v_granted;

    RETURN COALESCE(v_granted, false);
END;
$$;

CREATE FUNCTION public.create_monthly_partition(p_parent_table text, p_month_start date) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_partition_name TEXT := p_parent_table || '_' || to_char(p_month_start, 'YYYY_MM');
    v_month_end      DATE := (p_month_start + INTERVAL '1 month')::date;
BEGIN
    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L);',
        v_partition_name, p_parent_table, p_month_start, v_month_end
    );
END;
$$;

CREATE FUNCTION public.create_notification(p_title character varying, p_body text, p_channel character varying DEFAULT 'in_app'::character varying, p_template_id uuid DEFAULT NULL::uuid, p_user_ids uuid[] DEFAULT '{}'::uuid[], p_data jsonb DEFAULT '{}'::jsonb) RETURNS uuid
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_notification_id UUID;
    v_user_id UUID;
BEGIN
    INSERT INTO notifications (template_id, title, body, channel, data)
    VALUES (p_template_id, p_title, p_body, p_channel, p_data)
    RETURNING id INTO v_notification_id;

    FOREACH v_user_id IN ARRAY p_user_ids LOOP
        INSERT INTO user_notifications (notification_id, user_id)
        VALUES (v_notification_id, v_user_id)
        ON CONFLICT (notification_id, user_id) DO NOTHING;
    END LOOP;

    RETURN v_notification_id;
END;
$$;

CREATE FUNCTION public.create_user(p_username character varying, p_email character varying, p_password_hash text, p_first_name character varying DEFAULT NULL::character varying, p_last_name character varying DEFAULT NULL::character varying) RETURNS uuid
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_user_id UUID;
BEGIN
    INSERT INTO users (username, email, password_hash, first_name, last_name, password_changed_at)
    VALUES (p_username, p_email, p_password_hash, p_first_name, p_last_name, now())
    RETURNING id INTO v_user_id;

    RETURN v_user_id;
END;
$$;

CREATE FUNCTION public.fn_audit_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_before JSONB;
    v_after  JSONB;
    v_entity_id UUID;
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_before := to_jsonb(OLD);
        v_after  := NULL;
        v_entity_id := OLD.id;
    ELSIF TG_OP = 'UPDATE' THEN
        v_before := to_jsonb(OLD);
        v_after  := to_jsonb(NEW);
        v_entity_id := NEW.id;
    ELSE
        v_before := NULL;
        v_after  := to_jsonb(NEW);
        v_entity_id := NEW.id;
    END IF;

    INSERT INTO audit_logs (user_id, action, entity_type, entity_id, before_data, after_data)
    VALUES (
        NULLIF(current_setting('app.current_user_id', true), '')::uuid,
        lower(TG_OP),
        TG_TABLE_NAME,
        v_entity_id,
        v_before,
        v_after
    );

    RETURN COALESCE(NEW, OLD);
EXCEPTION
    WHEN invalid_text_representation THEN
        -- app.current_user_id was set to something that isn't a valid uuid;
        -- never let a malformed session var block the underlying write.
        INSERT INTO audit_logs (user_id, action, entity_type, entity_id, before_data, after_data)
        VALUES (NULL, lower(TG_OP), TG_TABLE_NAME, v_entity_id, v_before, v_after);
        RETURN COALESCE(NEW, OLD);
END;
$$;

CREATE FUNCTION public.fn_prevent_cycle() RETURNS trigger
    LANGUAGE plpgsql
    AS $_$
DECLARE
    v_parent_col   TEXT := TG_ARGV[0];
    v_current_id   UUID;
    v_next_id      UUID;
    v_depth        INT := 0;
BEGIN
    EXECUTE format('SELECT ($1).%I', v_parent_col) INTO v_next_id USING NEW;

    IF v_next_id IS NULL THEN
        RETURN NEW;
    END IF;

    IF v_next_id = NEW.id THEN
        RAISE EXCEPTION '% cannot be its own parent (id=%)', TG_TABLE_NAME, NEW.id;
    END IF;

    v_current_id := v_next_id;
    WHILE v_current_id IS NOT NULL LOOP
        v_depth := v_depth + 1;
        IF v_depth > 1000 THEN
            RAISE EXCEPTION 'parent chain too deep on % (possible existing cycle)', TG_TABLE_NAME;
        END IF;

        IF v_current_id = NEW.id THEN
            RAISE EXCEPTION 'cycle detected in % hierarchy: % is an ancestor of itself via %',
                TG_TABLE_NAME, NEW.id, v_parent_col;
        END IF;

        EXECUTE format('SELECT %I FROM %I WHERE id = $1', v_parent_col, TG_TABLE_NAME)
            INTO v_next_id USING v_current_id;
        v_current_id := v_next_id;
    END LOOP;

    RETURN NEW;
END;
$_$;

CREATE FUNCTION public.get_effective_feature_flag(p_key character varying, p_organization_id uuid DEFAULT NULL::uuid) RETURNS boolean
    LANGUAGE plpgsql STABLE
    AS $$
DECLARE
    v_org_value BOOLEAN;
    v_global_value BOOLEAN;
BEGIN
    IF p_organization_id IS NOT NULL THEN
        SELECT is_enabled INTO v_org_value
        FROM feature_flags
        WHERE key = p_key AND organization_id = p_organization_id;

        IF v_org_value IS NOT NULL THEN
            RETURN v_org_value;
        END IF;
    END IF;

    SELECT is_enabled INTO v_global_value
    FROM feature_flags
    WHERE key = p_key AND organization_id IS NULL;

    RETURN COALESCE(v_global_value, false);
END;
$$;

CREATE FUNCTION public.get_user_menus(p_user_id uuid) RETURNS TABLE(menu_item_id uuid, menu_id uuid, parent_menu_item_id uuid, label character varying, route_path character varying, module_key character varying, icon character varying, sort_order integer)
    LANGUAGE plpgsql STABLE
    AS $$
BEGIN
    RETURN QUERY
    SELECT DISTINCT
        mi.id, mi.menu_id, mi.parent_menu_item_id, mi.label,
        mi.route_path, mi.module_key, mi.icon, mi.sort_order
    FROM menu_items mi
    JOIN role_menus rm ON rm.menu_item_id = mi.id AND rm.can_view = true
    JOIN user_roles ur ON ur.role_id = rm.role_id AND ur.user_id = p_user_id
                       AND (ur.expires_at IS NULL OR ur.expires_at > now())
    WHERE mi.is_active = true AND mi.deleted_at IS NULL
      AND (
            NOT EXISTS (SELECT 1 FROM menu_permissions mp WHERE mp.menu_item_id = mi.id)
         OR EXISTS (
                SELECT 1 FROM menu_permissions mp
                JOIN permissions p ON p.id = mp.permission_id
                WHERE mp.menu_item_id = mi.id
                  AND check_permission(p_user_id, p.resource, p.action, ur.organization_id)
            )
      )
    ORDER BY mi.sort_order;
END;
$$;

CREATE FUNCTION public.handle_failed_login(p_user_id uuid, p_ip inet DEFAULT NULL::inet) RETURNS boolean
    LANGUAGE plpgsql
    AS $$  -- returns true if the account is now locked
DECLARE
    v_attempts INT;
BEGIN
    UPDATE users
       SET failed_login_attempts = failed_login_attempts + 1
     WHERE id = p_user_id
     RETURNING failed_login_attempts INTO v_attempts;

    INSERT INTO auth_logs (user_id, event_type, ip_address)
    VALUES (p_user_id, 'login_failed', p_ip);

    IF v_attempts >= 5 THEN
        UPDATE users SET locked_until = now() + INTERVAL '15 minutes' WHERE id = p_user_id;
        INSERT INTO auth_logs (user_id, event_type, ip_address)
        VALUES (p_user_id, 'account_locked', p_ip);
        RETURN true;
    END IF;

    RETURN false;
END;
$$;

CREATE FUNCTION public.handle_successful_login(p_user_id uuid, p_ip inet DEFAULT NULL::inet, p_user_agent text DEFAULT NULL::text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE users
       SET failed_login_attempts = 0,
           locked_until = NULL,
           last_login_at = now()
     WHERE id = p_user_id;

    INSERT INTO auth_logs (user_id, event_type, ip_address, user_agent)
    VALUES (p_user_id, 'login_success', p_ip, p_user_agent);

    INSERT INTO login_history (user_id, ip_address, user_agent, success)
    VALUES (p_user_id, p_ip, p_user_agent, true);
END;
$$;

CREATE FUNCTION public.log_audit_event(p_user_id uuid, p_action character varying, p_entity_type character varying, p_entity_id uuid, p_before_data jsonb DEFAULT NULL::jsonb, p_after_data jsonb DEFAULT NULL::jsonb, p_ip_address inet DEFAULT NULL::inet) RETURNS uuid
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id UUID;
BEGIN
    INSERT INTO audit_logs (user_id, action, entity_type, entity_id, before_data, after_data, ip_address)
    VALUES (p_user_id, p_action, p_entity_type, p_entity_id, p_before_data, p_after_data, p_ip_address)
    RETURNING id INTO v_id;

    RETURN v_id;
END;
$$;

CREATE FUNCTION public.refresh_user_permissions_mv() RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY user_permissions_mv;
END;
$$;

CREATE FUNCTION public.revoke_role(p_user_id uuid, p_role_id uuid, p_organization_id uuid DEFAULT NULL::uuid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM user_roles
    WHERE user_id = p_user_id AND role_id = p_role_id
      AND organization_id IS NOT DISTINCT FROM p_organization_id;
END;
$$;

CREATE FUNCTION public.revoke_session(p_session_id uuid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE sessions SET revoked_at = now() WHERE id = p_session_id AND revoked_at IS NULL;
END;
$$;

-- set_updated_at() is defined in V6__observability.sql (its first point of use in
-- migration-version order) -- not redefined here.

CREATE FUNCTION public.update_settings(p_scope character varying, p_organization_id uuid, p_key character varying, p_value jsonb) RETURNS uuid
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id UUID;
BEGIN
    IF p_scope = 'global' THEN
        INSERT INTO settings (scope, organization_id, key, value)
        VALUES ('global', NULL, p_key, p_value)
        ON CONFLICT (scope, key) WHERE organization_id IS NULL
        DO UPDATE SET value = EXCLUDED.value
        RETURNING id INTO v_id;
    ELSE
        INSERT INTO settings (scope, organization_id, key, value)
        VALUES ('organization', p_organization_id, p_key, p_value)
        ON CONFLICT (scope, organization_id, key) WHERE organization_id IS NOT NULL
        DO UPDATE SET value = EXCLUDED.value
        RETURNING id INTO v_id;
    END IF;

    RETURN v_id;
END;
$$;

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- fn_audit_trigger() -- audit-relevant tables
CREATE TRIGGER trg_audit_permissions AFTER INSERT OR DELETE OR UPDATE ON public.permissions FOR EACH ROW EXECUTE FUNCTION public.fn_audit_trigger();
CREATE TRIGGER trg_audit_role_permissions AFTER INSERT OR DELETE OR UPDATE ON public.role_permissions FOR EACH ROW EXECUTE FUNCTION public.fn_audit_trigger();
CREATE TRIGGER trg_audit_roles AFTER INSERT OR DELETE OR UPDATE ON public.roles FOR EACH ROW EXECUTE FUNCTION public.fn_audit_trigger();
CREATE TRIGGER trg_audit_settings AFTER INSERT OR DELETE OR UPDATE ON public.settings FOR EACH ROW EXECUTE FUNCTION public.fn_audit_trigger();
CREATE TRIGGER trg_audit_user_roles AFTER INSERT OR DELETE OR UPDATE ON public.user_roles FOR EACH ROW EXECUTE FUNCTION public.fn_audit_trigger();
CREATE TRIGGER trg_audit_users AFTER INSERT OR DELETE OR UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.fn_audit_trigger();

-- fn_prevent_cycle('<parent_column>') -- self-referencing hierarchy tables
CREATE TRIGGER trg_prevent_cycle_departments BEFORE INSERT OR UPDATE OF parent_department_id ON public.departments FOR EACH ROW EXECUTE FUNCTION public.fn_prevent_cycle('parent_department_id');
CREATE TRIGGER trg_prevent_cycle_menu_items BEFORE INSERT OR UPDATE OF parent_menu_item_id ON public.menu_items FOR EACH ROW EXECUTE FUNCTION public.fn_prevent_cycle('parent_menu_item_id');
CREATE TRIGGER trg_prevent_cycle_organizations BEFORE INSERT OR UPDATE OF parent_organization_id ON public.organizations FOR EACH ROW EXECUTE FUNCTION public.fn_prevent_cycle('parent_organization_id');

-- set_updated_at() -- every table with an updated_at column
CREATE TRIGGER trg_set_updated_at_api_keys BEFORE UPDATE ON public.api_keys FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_app_modules BEFORE UPDATE ON public.app_modules FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_attachments BEFORE UPDATE ON public.attachments FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_audit_logs BEFORE UPDATE ON public.audit_logs FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_auth_logs BEFORE UPDATE ON public.auth_logs FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_comments BEFORE UPDATE ON public.comments FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_departments BEFORE UPDATE ON public.departments FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_entity_tags BEFORE UPDATE ON public.entity_tags FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
-- trg_set_updated_at_error_logs already created in V6__observability.sql
CREATE TRIGGER trg_set_updated_at_feature_flags BEFORE UPDATE ON public.feature_flags FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_login_history BEFORE UPDATE ON public.login_history FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_menu_items BEFORE UPDATE ON public.menu_items FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_menu_permissions BEFORE UPDATE ON public.menu_permissions FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_menus BEFORE UPDATE ON public.menus FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_metadata_kv BEFORE UPDATE ON public.metadata_kv FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
-- trg_set_updated_at_notification_templates already created in V7__notifications.sql
-- trg_set_updated_at_notifications already created in V7__notifications.sql
CREATE TRIGGER trg_set_updated_at_organization_members BEFORE UPDATE ON public.organization_members FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_organizations BEFORE UPDATE ON public.organizations FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_permissions BEFORE UPDATE ON public.permissions FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_rate_limits BEFORE UPDATE ON public.rate_limits FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_role_menus BEFORE UPDATE ON public.role_menus FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_role_permissions BEFORE UPDATE ON public.role_permissions FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_roles BEFORE UPDATE ON public.roles FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_sessions BEFORE UPDATE ON public.sessions FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_settings BEFORE UPDATE ON public.settings FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
-- trg_set_updated_at_system_events already created in V6__observability.sql
CREATE TRIGGER trg_set_updated_at_tags BEFORE UPDATE ON public.tags FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_teams BEFORE UPDATE ON public.teams FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_user_identity_providers BEFORE UPDATE ON public.user_identity_providers FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
-- trg_set_updated_at_user_notifications already created in V7__notifications.sql
CREATE TRIGGER trg_set_updated_at_user_roles BEFORE UPDATE ON public.user_roles FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_set_updated_at_users BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
