import { useState, useMemo } from 'react';
import { Box, Button, Switch } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useRoleMenus, useDeleteRoleMenu, useUpdateRoleMenu } from '../hooks/useRoleMenus';
import { useRoles } from '@/features/roles/hooks/useRoles';
import { useMenuItems } from '@/features/menus/hooks/useMenus';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import RoleMenuFormDialog from '../components/RoleMenuFormDialog';
import type { RoleMenu } from '../api/roleMenusApi';

export default function RoleMenus() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const { data, isLoading, isError, refetch } = useRoleMenus({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { data: rolesPage } = useRoles({ page: 0, size: 100 });
  const { data: menuItemsPage } = useMenuItems({ page: 0, size: 200 });
  const { mutate: remove } = useDeleteRoleMenu();
  const { mutate: update } = useUpdateRoleMenu();
  const { confirm } = useConfirmStore();

  const roleName = useMemo(() => {
    const map = new Map((rolesPage?.items ?? []).map((r) => [r.id, r.name]));
    return (id?: string) => (id && map.get(id)) || id || '—';
  }, [rolesPage]);
  const menuItemLabel = useMemo(() => {
    const map = new Map((menuItemsPage?.items ?? []).map((mi) => [mi.id, mi.label]));
    return (id?: string) => (id && map.get(id)) || id || '—';
  }, [menuItemsPage]);

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((rm) => roleName(rm.roleId).toLowerCase().includes(term) || menuItemLabel(rm.menuItemId).toLowerCase().includes(term));
  }, [data, search, roleName, menuItemLabel]);

  const columns: ColumnDef<RoleMenu>[] = useMemo(() => [
    { field: 'roleId', headerName: 'Role', width: 160, renderCell: (row) => roleName(row.roleId) },
    { field: 'menuItemId', headerName: 'Menu item', width: 220, renderCell: (row) => menuItemLabel(row.menuItemId) },
    {
      field: 'canView', headerName: 'Can view', width: 110,
      renderCell: (row) => <Switch checked={row.canView ?? false} size="small" onChange={() => update({ id: row.id!, body: { canView: !row.canView } })} />,
    },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            {
              label: 'Revoke', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Revoke role menu access', message: 'Remove this role\'s access grant for the menu item?', confirmLabel: 'Revoke', severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [confirm, remove, update, roleName, menuItemLabel]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Role Menu Access"
        description="Which menu items each role can see."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Grant Access</Button>}
      />
      <ServerDataTable
        title="Role Menu Access"
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
      <RoleMenuFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </Box>
  );
}
