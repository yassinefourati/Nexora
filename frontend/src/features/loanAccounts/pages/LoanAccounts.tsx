import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EventNoteIcon from '@mui/icons-material/EventNote';
import LockIcon from '@mui/icons-material/Lock';
import ReportProblemIcon from '@mui/icons-material/ReportProblem';
import AddIcon from '@mui/icons-material/Add';
import { useLoanAccounts, useDeleteLoanAccount } from '../hooks/useLoanAccounts';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import LoanAccountFormDialog from '../components/LoanAccountFormDialog';
import CloseLoanAccountDialog from '../components/CloseLoanAccountDialog';
import DefaultLoanAccountDialog from '../components/DefaultLoanAccountDialog';
import InstallmentScheduleDialog from '../components/InstallmentScheduleDialog';
import type { LoanAccount } from '../api/loanAccountsApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  active: 'success',
  closed: 'default',
  defaulted: 'error',
};

export default function LoanAccounts() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [closeTarget, setCloseTarget] = useState<LoanAccount | null>(null);
  const [defaultTarget, setDefaultTarget] = useState<LoanAccount | null>(null);
  const [scheduleTarget, setScheduleTarget] = useState<LoanAccount | null>(null);

  const { data, isLoading, isError, refetch } = useLoanAccounts({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteLoanAccount();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((a) => a.accountNumber?.toLowerCase().includes(term) || a.status?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<LoanAccount>[] = useMemo(() => [
    { field: 'accountNumber', headerName: 'Account #', width: 150 },
    { field: 'status', headerName: 'Status', width: 130, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'active']} /> },
    { field: 'principalAmount', headerName: 'Principal', width: 130 },
    { field: 'outstandingPrincipal', headerName: 'Outstanding', width: 130 },
    { field: 'termMonths', headerName: 'Term (mo)', width: 110 },
    {
      field: 'actions', headerName: '', width: 120, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: 'View schedule', icon: <EventNoteIcon fontSize="small" />, onClick: () => setScheduleTarget(row) },
            ...(row.status === 'active' ? [
              { label: 'Close', icon: <LockIcon fontSize="small" />, onClick: () => setCloseTarget(row) },
              { label: 'Mark defaulted', icon: <ReportProblemIcon fontSize="small" color="error" />, color: 'error' as const, onClick: () => setDefaultTarget(row) },
            ] : []),
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete loan account',
                message: 'Delete this loan account?',
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
        title="Loan Accounts"
        description="Servicing accounts opened once a loan disbursement completes, with their generated repayment schedule."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Open Loan Account</Button>}
      />
      <ServerDataTable
        title="Loan Accounts"
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
      <LoanAccountFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
      <CloseLoanAccountDialog open={!!closeTarget} onClose={() => setCloseTarget(null)} loanAccount={closeTarget} />
      <DefaultLoanAccountDialog open={!!defaultTarget} onClose={() => setDefaultTarget(null)} loanAccount={defaultTarget} />
      <InstallmentScheduleDialog open={!!scheduleTarget} onClose={() => setScheduleTarget(null)} loanAccount={scheduleTarget} />
    </Box>
  );
}
