import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import GavelIcon from '@mui/icons-material/Gavel';
import AddIcon from '@mui/icons-material/Add';
import { useUnderwritingCases, useDeleteUnderwritingCase, useStartReviewUnderwritingCase } from '../hooks/useUnderwritingCases';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import UnderwritingCaseFormDialog from '../components/UnderwritingCaseFormDialog';
import DecideUnderwritingCaseDialog from '../components/DecideUnderwritingCaseDialog';
import type { UnderwritingCase } from '../api/underwritingCasesApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  pending: 'default',
  in_review: 'warning',
  completed: 'success',
};

const DECISION_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  approve: 'success',
  approve_with_conditions: 'info',
  refer: 'warning',
  reject: 'error',
  request_information: 'warning',
};

export default function UnderwritingCases() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [decideCase, setDecideCase] = useState<UnderwritingCase | null>(null);

  const { data, isLoading, isError, refetch } = useUnderwritingCases({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteUnderwritingCase();
  const { mutate: startReview } = useStartReviewUnderwritingCase();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((c) => c.status?.toLowerCase().includes(term) || c.decision?.toLowerCase().includes(term) || c.assignedTo?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<UnderwritingCase>[] = useMemo(() => [
    { field: 'assignedTo', headerName: 'Assigned to', width: 160 },
    { field: 'status', headerName: 'Status', width: 130, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'pending']} /> },
    { field: 'decision', headerName: 'Decision', width: 200, renderCell: (row) => row.decision ? <Chip label={row.decision.replace(/_/g, ' ')} size="small" color={DECISION_COLOR[row.decision]} /> : '—' },
    { field: 'approvedAmount', headerName: 'Approved amount', width: 150 },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'pending' ? [
              { label: 'Start review', icon: <PlayArrowIcon fontSize="small" />, onClick: () => startReview(row.id!) },
            ] : []),
            ...(row.status === 'in_review' ? [
              { label: 'Decide', icon: <GavelIcon fontSize="small" />, onClick: () => setDecideCase(row) },
            ] : []),
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete underwriting case',
                message: 'Delete this underwriting case?',
                confirmLabel: 'Delete', severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [confirm, remove, startReview]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Underwriting Cases"
        description="Review credit, risk and fraud outcomes and record the lending decision for each loan application."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Open Underwriting Case</Button>}
      />
      <ServerDataTable
        title="Underwriting Cases"
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
      <UnderwritingCaseFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
      <DecideUnderwritingCaseDialog open={!!decideCase} onClose={() => setDecideCase(null)} underwritingCase={decideCase} />
    </Box>
  );
}
