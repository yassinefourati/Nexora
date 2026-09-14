import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useMenuPermissions, useDeleteMenuPermission } from '../hooks/useMenuPermissions';
import MenuPermissionFormDialog from '../components/MenuPermissionFormDialog';
import type { MenuPermission } from '../api/menuPermissionsApi';
import { useMenuItems } from '@/features/menus/hooks/useMenus';
import { usePermissionsList } from '@/features/permissions/hooks/usePermissions';

export default function MenuPermissions() {
  const { t } = useTranslation();
  const { data: menuItemsPage } = useMenuItems({ page: 0, size: 200 });
  const { data: permissionsPage } = usePermissionsList({ page: 0, size: 200 });
  const menuItemLabel = useMemo(() => { const m = new Map((menuItemsPage?.items ?? []).map((mi) => [mi.id, mi.label])); return (id?: string) => (id && m.get(id)) || id || t('common.dash'); }, [menuItemsPage, t]);
  const permissionCode = useMemo(() => { const m = new Map((permissionsPage?.items ?? []).map((p) => [p.id, p.code])); return (id?: string) => (id && m.get(id)) || id || t('common.dash'); }, [permissionsPage, t]);
  const columns: ColumnDef<MenuPermission>[] = useMemo(() => [
    { field: 'menuItemId', headerName: t('fields.menuItem'), width: 220, renderCell: (row) => menuItemLabel(row.menuItemId) },
    { field: 'permissionId', headerName: t('fields.permission'), width: 220, renderCell: (row) => permissionCode(row.permissionId) },
  ], [t, menuItemLabel, permissionCode]);

  return (
    <CrudPage<MenuPermission>
      resource="menuPermissions"
      permission="menu"
      useList={useMenuPermissions}
      useRemove={useDeleteMenuPermission}
      columns={columns}
      rowLabel={(row) => `${menuItemLabel(row.menuItemId)} / ${permissionCode(row.permissionId)}`}
      editable={false}
      deleteActionKey="common.revoke"
      renderDialog={({ open, onClose }) => <MenuPermissionFormDialog open={open} onClose={onClose} />}
    ></CrudPage>
  );
}
