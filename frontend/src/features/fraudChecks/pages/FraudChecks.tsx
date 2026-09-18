import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import AddIcon from '@mui/icons-material/Add';
import { useFraudChecks, useProcessFraudCheck } from '../hooks/useFraudChecks';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import FraudCheckFormDialog from '../components/FraudCheckFormDialog';
import type { FraudCheck } from '../api/fraudChecksApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  pending: 'default',
  in_progress: 'info',
  clear: 'success',
  flagged: 'error',
};

export default function FraudChecks() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const { data, isLoading, isError, refetch } = useFraudChecks({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: process } = useProcessFraudCheck();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((f) => f.status?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<FraudCheck>[] = useMemo(() => [
    { field: 'status', headerName: 'Status', width: 140, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'pending']} /> },
    { field: 'fraudScore', headerName: 'Score', width: 100 },
    { field: 'checkedAt', headerName: 'Checked', width: 200, renderCell: (row) => row.checkedAt ? new Date(row.checkedAt).toLocaleString() : '—' },
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
        title="Fraud Checks"
        description="Manage fraud screening performed for loan applications."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Open Fraud Check</Button>}
      />
      <ServerDataTable
        title="Fraud Checks"
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
      <FraudCheckFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </Box>
  );
}
