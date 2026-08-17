import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import AddIcon from '@mui/icons-material/Add';
import { useCreditChecks, useProcessCreditCheck } from '../hooks/useCreditChecks';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import CreditCheckFormDialog from '../components/CreditCheckFormDialog';
import type { CreditCheck } from '../api/creditChecksApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  pending: 'default',
  in_progress: 'info',
  completed: 'success',
  failed: 'error',
};

export default function CreditChecks() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const { data, isLoading, isError, refetch } = useCreditChecks({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: process } = useProcessCreditCheck();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((c) => c.status?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<CreditCheck>[] = useMemo(() => [
    { field: 'status', headerName: 'Status', width: 150, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'pending']} /> },
    { field: 'requestedAt', headerName: 'Requested', width: 200, renderCell: (row) => row.requestedAt ? new Date(row.requestedAt).toLocaleString() : '' },
    { field: 'completedAt', headerName: 'Completed', width: 200, renderCell: (row) => row.completedAt ? new Date(row.completedAt).toLocaleString() : '—' },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'pending' ? [
              { label: 'Process', icon: <PlayArrowIcon fontSize="small" />, onClick: () => process(row.id!) },
            ] : []),
          ]}
        />
      ),
    },
  ], [process]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Credit Checks"
        description="Manage credit bureau checks performed for loan applications."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Request Credit Check</Button>}
      />
      <ServerDataTable
        title="Credit Checks"
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
      <CreditCheckFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </Box>
  );
}
