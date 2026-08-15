-- V4__menus.sql
-- Menus table group: menus, menu_items, role_menus, menu_permissions

-- menus
CREATE TABLE menus (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    name            VARCHAR(150)    NOT NULL,
    code            VARCHAR(100)    NOT NULL,
    description     TEXT,
    is_active       BOOLEAN         NOT NULL DEFAULT true,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT menus_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_menus_code ON menus USING btree (code) WHERE (deleted_at IS NULL);

-- menu_items
CREATE TABLE menu_items (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    menu_id             UUID            NOT NULL,
    parent_menu_item_id UUID,
    label               VARCHAR(150)    NOT NULL,
    route_path          VARCHAR(255),
    module_key          VARCHAR(100),
    icon                VARCHAR(100),
    sort_order          INTEGER         NOT NULL DEFAULT 0,
    is_active           BOOLEAN         NOT NULL DEFAULT true,
    deleted_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT menu_items_pkey PRIMARY KEY (id),
    CONSTRAINT menu_items_menu_id_fkey FOREIGN KEY (menu_id) REFERENCES menus (id) ON DELETE CASCADE,
    CONSTRAINT menu_items_parent_menu_item_id_fkey FOREIGN KEY (parent_menu_item_id) REFERENCES menu_items (id) ON DELETE CASCADE
);

CREATE INDEX idx_menu_items_menu ON menu_items USING btree (menu_id);
CREATE INDEX idx_menu_items_parent ON menu_items USING btree (parent_menu_item_id);

-- role_menus
CREATE TABLE role_menus (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    role_id         UUID            NOT NULL,
    menu_item_id    UUID            NOT NULL,
    can_view        BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT role_menus_pkey PRIMARY KEY (id),
    CONSTRAINT role_menus_role_id_menu_item_id_key UNIQUE (role_id, menu_item_id),
    CONSTRAINT role_menus_role_id_fkey FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT role_menus_menu_item_id_fkey FOREIGN KEY (menu_item_id) REFERENCES menu_items (id) ON DELETE CASCADE
);

CREATE INDEX idx_role_menus_role ON role_menus USING btree (role_id);
CREATE INDEX idx_role_menus_menu_item ON role_menus USING btree (menu_item_id);

-- menu_permissions
CREATE TABLE menu_permissions (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    menu_item_id    UUID            NOT NULL,
    permission_id   UUID            NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT menu_permissions_pkey PRIMARY KEY (id),
    CONSTRAINT menu_permissions_menu_item_id_permission_id_key UNIQUE (menu_item_id, permission_id),
    CONSTRAINT menu_permissions_menu_item_id_fkey FOREIGN KEY (menu_item_id) REFERENCES menu_items (id) ON DELETE CASCADE,
    CONSTRAINT menu_permissions_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

CREATE INDEX idx_menu_permissions_menu_item ON menu_permissions USING btree (menu_item_id);
