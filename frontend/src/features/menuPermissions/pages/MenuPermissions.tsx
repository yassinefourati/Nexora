import { useState, useMemo } from 'react';
import { Box, Button } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useMenuPermissions, useDeleteMenuPermission } from '../hooks/useMenuPermissions';
import { useMenuItems } from '@/features/menus/hooks/useMenus';
import { usePermissionsList } from '@/features/permissions/hooks/usePermissions';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import MenuPermissionFormDialog from '../components/MenuPermissionFormDialog';
import type { MenuPermission } from '../api/menuPermissionsApi';

export default function MenuPermissions() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const { data, isLoading, isError, refetch } = useMenuPermissions({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { data: menuItemsPage } = useMenuItems({ page: 0, size: 200 });
  const { data: permissionsPage } = usePermissionsList({ page: 0, size: 200 });
  const { mutate: remove } = useDeleteMenuPermission();
  const { confirm } = useConfirmStore();

  const menuItemLabel = useMemo(() => {
    const map = new Map((menuItemsPage?.items ?? []).map((mi) => [mi.id, mi.label]));
    return (id?: string) => (id && map.get(id)) || id || '—';
  }, [menuItemsPage]);
  const permissionCode = useMemo(() => {
    const map = new Map((permissionsPage?.items ?? []).map((p) => [p.id, p.code]));
    return (id?: string) => (id && map.get(id)) || id || '—';
  }, [permissionsPage]);

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((mp) => menuItemLabel(mp.menuItemId).toLowerCase().includes(term) || permissionCode(mp.permissionId).toLowerCase().includes(term));
  }, [data, search, menuItemLabel, permissionCode]);

  const columns: ColumnDef<MenuPermission>[] = useMemo(() => [
    { field: 'menuItemId', headerName: 'Menu item', width: 220, renderCell: (row) => menuItemLabel(row.menuItemId) },
    { field: 'permissionId', headerName: 'Permission', width: 220, renderCell: (row) => permissionCode(row.permissionId) },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            {
              label: 'Revoke', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Revoke menu permission',
                message: `Revoke this permission grant? Users without it will no longer see the menu item.`,
                confirmLabel: 'Revoke', severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [confirm, remove, menuItemLabel, permissionCode]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Menu Permissions"
        description="Permissions required to see each menu item."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Grant Permission</Button>}
      />
      <ServerDataTable
        title="Menu Permissions"
        rows={rows}
        columns={columns}
        pagination={data?.pagination}
        isLoading={isLoading}
        isError={isError}
        onRefetch={() => void refetch()}
        page={page}
        pageSize={pageSize}
        onPageChange={setPage}
        onPageSizeChange={setPageSize}
        sort={sort}
        onSortChange={(s) => { setSort(s); setPage(0); }}
        search={search}
        onSearchChange={setSearch}
        rowKey={(row) => row.id!}
      />
      <MenuPermissionFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </Box>
  );
}
