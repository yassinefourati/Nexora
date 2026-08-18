import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import { useLoanRepayments } from '../hooks/useLoanRepayments';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import LoanRepaymentFormDialog from '../components/LoanRepaymentFormDialog';
import CompleteLoanRepaymentDialog from '../components/CompleteLoanRepaymentDialog';
import FailLoanRepaymentDialog from '../components/FailLoanRepaymentDialog';
import type { LoanRepayment } from '../api/loanRepaymentsApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  pending: 'default',
  completed: 'success',
  failed: 'error',
};

export default function LoanRepayments() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [completeTarget, setCompleteTarget] = useState<LoanRepayment | null>(null);
  const [failTarget, setFailTarget] = useState<LoanRepayment | null>(null);

  const { data, isLoading, isError, refetch } = useLoanRepayments({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((r) => r.status?.toLowerCase().includes(term) || r.paymentMethod?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<LoanRepayment>[] = useMemo(() => [
    { field: 'status', headerName: 'Status', width: 130, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'pending']} /> },
    { field: 'amount', headerName: 'Amount', width: 130 },
    { field: 'paymentMethod', headerName: 'Method', width: 140 },
    { field: 'referenceNumber', headerName: 'Reference', width: 160 },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'pending' ? [
              { label: 'Complete', icon: <CheckCircleIcon fontSize="small" />, onClick: () => setCompleteTarget(row) },
              { label: 'Mark failed', icon: <CancelIcon fontSize="small" color="error" />, color: 'error' as const, onClick: () => setFailTarget(row) },
            ] : []),
          ]}
        />
      ),
    },
  ], []);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Loan Repayments"
        description="Customer payments captured against a loan account's installment schedule."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Record Repayment</Button>}
      />
      <ServerDataTable
        title="Loan Repayments"
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
      <LoanRepaymentFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
      <CompleteLoanRepaymentDialog open={!!completeTarget} onClose={() => setCompleteTarget(null)} loanRepayment={completeTarget} />
      <FailLoanRepaymentDialog open={!!failTarget} onClose={() => setFailTarget(null)} loanRepayment={failTarget} />
    </Box>
  );
}
