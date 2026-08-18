import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import { useLoanDisbursements, useDeleteLoanDisbursement, useInitiateLoanDisbursement } from '../hooks/useLoanDisbursements';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import LoanDisbursementFormDialog from '../components/LoanDisbursementFormDialog';
import CompleteLoanDisbursementDialog from '../components/CompleteLoanDisbursementDialog';
import FailLoanDisbursementDialog from '../components/FailLoanDisbursementDialog';
import type { LoanDisbursement } from '../api/loanDisbursementsApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  pending: 'default',
  initiated: 'info',
  completed: 'success',
  failed: 'error',
};

export default function LoanDisbursements() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [completeTarget, setCompleteTarget] = useState<LoanDisbursement | null>(null);
  const [failTarget, setFailTarget] = useState<LoanDisbursement | null>(null);

  const { data, isLoading, isError, refetch } = useLoanDisbursements({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteLoanDisbursement();
  const { mutate: initiate } = useInitiateLoanDisbursement();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((d) => d.status?.toLowerCase().includes(term) || d.destinationAccount?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<LoanDisbursement>[] = useMemo(() => [
    { field: 'status', headerName: 'Status', width: 130, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'pending']} /> },
    { field: 'amount', headerName: 'Amount', width: 130 },
    { field: 'disbursementMethod', headerName: 'Method', width: 140 },
    { field: 'destinationAccount', headerName: 'Destination', width: 160 },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'pending' ? [
              { label: 'Initiate', icon: <PlayArrowIcon fontSize="small" />, onClick: () => initiate(row.id!) },
            ] : []),
            ...(row.status === 'initiated' ? [
              { label: 'Complete', icon: <CheckCircleIcon fontSize="small" />, onClick: () => setCompleteTarget(row) },
              { label: 'Mark failed', icon: <CancelIcon fontSize="small" color="error" />, color: 'error' as const, onClick: () => setFailTarget(row) },
            ] : []),
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete loan disbursement',
                message: 'Delete this loan disbursement?',
                confirmLabel: 'Delete', severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [confirm, remove, initiate]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Loan Disbursements"
        description="Release principal funds for a loan application once its contract is fully signed."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Create Disbursement</Button>}
      />
      <ServerDataTable
        title="Loan Disbursements"
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
      <LoanDisbursementFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
      <CompleteLoanDisbursementDialog open={!!completeTarget} onClose={() => setCompleteTarget(null)} loanDisbursement={completeTarget} />
      <FailLoanDisbursementDialog open={!!failTarget} onClose={() => setFailTarget(null)} loanDisbursement={failTarget} />
    </Box>
  );
}
