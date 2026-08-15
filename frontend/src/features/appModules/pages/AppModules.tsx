import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useAppModules, useDeleteAppModule } from '../hooks/useAppModules';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import AppModuleFormDialog from '../components/AppModuleFormDialog';
import type { AppModule } from '../api/appModulesApi';

export default function AppModules() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editModule, setEditModule] = useState<AppModule | null>(null);

  const { data, isLoading, isError, refetch } = useAppModules({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteAppModule();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((m) => m.name?.toLowerCase().includes(term) || m.key?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<AppModule>[] = useMemo(() => [
    { field: 'key', headerName: 'Key', width: 160 },
    { field: 'name', headerName: 'Name', width: 200 },
    { field: 'description', headerName: 'Description', width: 260 },
    { field: 'active', headerName: 'Active', width: 100, renderCell: (row) => <Chip label={row.active ? 'Active' : 'Inactive'} size="small" color={row.active ? 'success' : 'default'} /> },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditModule(row); setDialogOpen(true); } },
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete app module',
                message: `Delete module "${row.name}"?`,
                confirmLabel: 'Delete', severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [confirm, remove]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="App Modules"
        description="Feature modules that can be enabled and referenced by menu items."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditModule(null); setDialogOpen(true); }}>Add Module</Button>}
      />
      <ServerDataTable
        title="App Modules"
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
      <AppModuleFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditModule(null); }} editModule={editModule} />
    </Box>
  );
}
