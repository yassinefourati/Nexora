import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useRoleMenus, useDeleteRoleMenu } from '../hooks/useRoleMenus';
import RoleMenuFormDialog from '../components/RoleMenuFormDialog';
import type { RoleMenu } from '../api/roleMenusApi';
import { Switch } from '@mui/material';
import { useUpdateRoleMenu } from '../hooks/useRoleMenus';
import { useRoles } from '@/features/roles/hooks/useRoles';
import { useMenuItems } from '@/features/menus/hooks/useMenus';

export default function RoleMenus() {
  const { t } = useTranslation();
  const { data: rolesPage } = useRoles({ page: 0, size: 100 });
  const { data: menuItemsPage } = useMenuItems({ page: 0, size: 200 });
  const { mutate: update } = useUpdateRoleMenu();
  const roleName = useMemo(() => { const m = new Map((rolesPage?.items ?? []).map((r) => [r.id, r.name])); return (id?: string) => (id && m.get(id)) || id || t('common.dash'); }, [rolesPage, t]);
  const menuItemLabel = useMemo(() => { const m = new Map((menuItemsPage?.items ?? []).map((mi) => [mi.id, mi.label])); return (id?: string) => (id && m.get(id)) || id || t('common.dash'); }, [menuItemsPage, t]);
  const columns: ColumnDef<RoleMenu>[] = useMemo(() => [
    { field: 'roleId', headerName: t('fields.role'), width: 160, renderCell: (row) => roleName(row.roleId) },
    { field: 'menuItemId', headerName: t('fields.menuItem'), width: 220, renderCell: (row) => menuItemLabel(row.menuItemId) },
    { field: 'canView', headerName: t('fields.canView'), width: 110, renderCell: (row) => <Switch checked={row.canView ?? false} size="small" inputProps={{ 'aria-label': t('fields.canView') }} onChange={() => update({ id: row.id!, body: { canView: !row.canView, version: row.version ?? undefined } })} /> },
  ], [t, roleName, menuItemLabel, update]);

  return (
    <CrudPage<RoleMenu>
      resource="roleMenus"
      permission="menu"
      useList={useRoleMenus}
      useRemove={useDeleteRoleMenu}
      columns={columns}
      rowLabel={(row) => `${roleName(row.roleId)} / ${menuItemLabel(row.menuItemId)}`}
      editable={false}
      deleteActionKey="common.revoke"
      renderDialog={({ open, onClose }) => <RoleMenuFormDialog open={open} onClose={onClose} />}
    ></CrudPage>
  );
}
