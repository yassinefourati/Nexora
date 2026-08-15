import { useState, useMemo } from 'react';
import { Box, Button, Switch } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useFeatureFlags, useDeleteFeatureFlag, useUpdateFeatureFlag } from '../hooks/useFeatureFlags';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import FeatureFlagFormDialog from '../components/FeatureFlagFormDialog';
import type { FeatureFlag } from '../api/featureFlagsApi';

export default function FeatureFlags() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editFlag, setEditFlag] = useState<FeatureFlag | null>(null);

  const { data, isLoading, isError, refetch } = useFeatureFlags({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteFeatureFlag();
  const { mutate: update } = useUpdateFeatureFlag();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((f) => f.key?.toLowerCase().includes(term) || f.name?.toLowerCase().includes(term));
  }, [data, search]);

  const toggle = (flag: FeatureFlag) => {
    update({ id: flag.id!, body: { name: flag.name!, description: flag.description, enabled: !flag.enabled } });
  };

  const columns: ColumnDef<FeatureFlag>[] = useMemo(() => [
    { field: 'key', headerName: 'Key', width: 180 },
    { field: 'name', headerName: 'Name', width: 200 },
    { field: 'description', headerName: 'Description', width: 240 },
    { field: 'enabled', headerName: 'Enabled', width: 100, renderCell: (row) => <Switch checked={row.enabled ?? false} size="small" onChange={() => toggle(row)} /> },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditFlag(row); setDialogOpen(true); } },
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete feature flag', message: `Delete flag "${row.name}"?`, confirmLabel: 'Delete', severity: 'error',
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
        title="Feature Flags"
        description="Toggle features globally or per organization."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditFlag(null); setDialogOpen(true); }}>Add Flag</Button>}
      />
      <ServerDataTable
        title="Feature Flags"
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
      <FeatureFlagFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditFlag(null); }} editFlag={editFlag} />
    </Box>
  );
}
