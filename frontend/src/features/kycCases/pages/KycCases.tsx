import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import { useKycCases, useStartKycReview, useCompleteKycCase } from '../hooks/useKycCases';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import KycCaseFormDialog from '../components/KycCaseFormDialog';
import type { KycCase } from '../api/kycCasesApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  pending: 'default',
  in_progress: 'info',
  passed: 'success',
  failed: 'error',
  manual_review: 'warning',
  expired: 'default',
};

export default function KycCases() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const { data, isLoading, isError, refetch } = useKycCases({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: startReview } = useStartKycReview();
  const { mutate: complete } = useCompleteKycCase();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((c) => c.status?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<KycCase>[] = useMemo(() => [
    { field: 'status', headerName: 'Status', width: 150, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'pending']} /> },
    { field: 'initiatedAt', headerName: 'Initiated', width: 200, renderCell: (row) => row.initiatedAt ? new Date(row.initiatedAt).toLocaleString() : '' },
    { field: 'completedAt', headerName: 'Completed', width: 200, renderCell: (row) => row.completedAt ? new Date(row.completedAt).toLocaleString() : '—' },
    {
      field: 'actions', headerName: '', width: 130, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'pending' ? [
              { label: 'Start review', icon: <PlayArrowIcon fontSize="small" />, onClick: () => startReview(row.id!) },
            ] : []),
            ...(row.status === 'pending' || row.status === 'in_progress' ? [
              { label: 'Pass', icon: <CheckCircleIcon fontSize="small" color="success" />, onClick: () => complete({ id: row.id!, outcome: 'passed' }) },
              { label: 'Fail', icon: <CancelIcon fontSize="small" color="error" />, color: 'error' as const, onClick: () => complete({ id: row.id!, outcome: 'failed' }) },
            ] : []),
          ]}
        />
      ),
    },
  ], [startReview, complete]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="KYC Cases"
        description="Manage customer identity-verification (Know Your Customer) cases."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Open KYC Case</Button>}
      />
      <ServerDataTable
        title="KYC Cases"
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
      <KycCaseFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </Box>
  );
}
