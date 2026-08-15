import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useTranslation } from 'react-i18next';
import { usePermissionsList, useDeletePermission } from '../hooks/usePermissions';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import Can from '@/shared/components/Can';
import { usePermission } from '@/shared/hooks/usePermission';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import PageHeader from '@/shared/components/PageHeader';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import PermissionFormDialog from '../components/PermissionFormDialog';
import type { Permission } from '../api/permissionsApi';

export default function Permissions() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const { data, isLoading, isError, refetch } = usePermissionsList({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });

  const { mutate: remove } = useDeletePermission();
  const { confirm } = useConfirmStore();
  const canCreate = usePermission('role', 'write');
  const canDelete = usePermission('role', 'delete');

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((p) => p.resource?.toLowerCase().includes(term) || p.action?.toLowerCase().includes(term) || p.code?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<Permission>[] = useMemo(() => [
    { field: 'code', headerName: 'Code', width: 220 },
    { field: 'resource', headerName: 'Resource', width: 160 },
    { field: 'action', headerName: 'Action', width: 120, renderCell: (row) => <Chip label={row.action} size="small" variant="outlined" /> },
    { field: 'description', headerName: 'Description', width: 260 },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            {
              label: t('common.delete'),
              icon: <DeleteIcon fontSize="small" color="error" />,
              color: 'error',
              hidden: !canDelete,
              onClick: () => confirm({
                title: 'Delete permission',
                message: `Delete permission "${row.code}"? Any role grants referencing it will also be removed.`,
                confirmLabel: t('common.delete'),
                severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [t, confirm, remove, canDelete]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title={t('menu.permissions')}
        description="Permission catalog — resource/action pairs that roles can be granted."
        actions={
          <Can resource="role" action="write">
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)} disabled={!canCreate}>
              Add Permission
            </Button>
          </Can>
        }
      />

      <ServerDataTable
        title={t('menu.permissions')}
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

      <PermissionFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </Box>
  );
}
