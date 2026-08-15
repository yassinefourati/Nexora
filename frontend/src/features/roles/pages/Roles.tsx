import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useTranslation } from 'react-i18next';
import { useRoles, useDeleteRole } from '../hooks/useRoles';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import Can from '@/shared/components/Can';
import { usePermission } from '@/shared/hooks/usePermission';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import PageHeader from '@/shared/components/PageHeader';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import RoleFormDialog from '../components/RoleFormDialog';
import type { Role } from '../api/rolesApi';

export default function Roles() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editRole, setEditRole] = useState<Role | null>(null);

  const { data, isLoading, isError, refetch } = useRoles({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });

  const { mutate: remove } = useDeleteRole();
  const { confirm } = useConfirmStore();
  const canEdit = usePermission('role', 'edit');
  const canDelete = usePermission('role', 'delete');

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((r) => r.name?.toLowerCase().includes(term) || r.description?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<Role>[] = useMemo(() => [
    { field: 'name', headerName: 'Name', width: 180 },
    { field: 'description', headerName: 'Description', width: 300 },
    {
      field: 'system', headerName: 'System', width: 110,
      renderCell: (row) => row.system ? <Chip label="System" size="small" color="warning" variant="outlined" /> : '—',
    },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            {
              label: t('common.edit'),
              icon: <EditIcon fontSize="small" />,
              hidden: !canEdit || row.system,
              onClick: () => { setEditRole(row); setDialogOpen(true); },
            },
            {
              label: t('common.delete'),
              icon: <DeleteIcon fontSize="small" color="error" />,
              color: 'error',
              hidden: !canDelete || row.system,
              onClick: () => confirm({
                title: 'Delete role',
                message: `Delete role "${row.name}"? This cannot be undone.`,
                confirmLabel: t('common.delete'),
                severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [t, confirm, remove, canEdit, canDelete]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title={t('menu.roles')}
        description="Manage roles that can be assigned to users."
        actions={
          <Can resource="role" action="write">
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditRole(null); setDialogOpen(true); }}>
              Add Role
            </Button>
          </Can>
        }
      />

      <ServerDataTable
        title={t('menu.roles')}
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

      <RoleFormDialog
        open={dialogOpen}
        onClose={() => { setDialogOpen(false); setEditRole(null); }}
        editRole={editRole}
      />
    </Box>
  );
}
