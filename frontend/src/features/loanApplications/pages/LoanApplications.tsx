import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import SendIcon from '@mui/icons-material/Send';
import CancelIcon from '@mui/icons-material/Cancel';
import { useLoanApplications, useDeleteLoanApplication, useSubmitLoanApplication, useCancelLoanApplication } from '../hooks/useLoanApplications';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import LoanApplicationFormDialog from '../components/LoanApplicationFormDialog';
import type { LoanApplication } from '../api/loanApplicationsApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  draft: 'default',
  submitted: 'info',
  under_review: 'warning',
  approved: 'success',
  rejected: 'error',
  withdrawn: 'default',
  cancelled: 'default',
};

export default function LoanApplications() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editApplication, setEditApplication] = useState<LoanApplication | null>(null);

  const { data, isLoading, isError, refetch } = useLoanApplications({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteLoanApplication();
  const { mutate: submit } = useSubmitLoanApplication();
  const { mutate: cancel } = useCancelLoanApplication();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((a) => a.purpose?.toLowerCase().includes(term) || a.status?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<LoanApplication>[] = useMemo(() => [
    { field: 'requestedAmount', headerName: 'Amount', width: 130 },
    { field: 'requestedTermMonths', headerName: 'Term (mo)', width: 110 },
    { field: 'purpose', headerName: 'Purpose', width: 200 },
    { field: 'status', headerName: 'Status', width: 140, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'draft']} /> },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'draft' ? [
              { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditApplication(row); setDialogOpen(true); } },
              { label: 'Submit', icon: <SendIcon fontSize="small" />, onClick: () => submit(row.id!) },
            ] : []),
            ...(!['approved', 'rejected', 'cancelled', 'withdrawn'].includes(row.status ?? '') ? [
              {
                label: 'Cancel', icon: <CancelIcon fontSize="small" color="error" />, color: 'error' as const,
                onClick: () => confirm({
                  title: 'Cancel loan application',
                  message: 'Cancel this loan application?',
                  confirmLabel: 'Cancel application', severity: 'error',
                  onConfirm: () => cancel({ id: row.id! }),
                }),
              },
            ] : []),
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete loan application',
                message: 'Delete this loan application?',
                confirmLabel: 'Delete', severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [confirm, remove, submit, cancel]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Loan Applications"
        description="Manage customer applications for a loan product."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditApplication(null); setDialogOpen(true); }}>Add Loan Application</Button>}
      />
      <ServerDataTable
        title="Loan Applications"
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
      <LoanApplicationFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditApplication(null); }} editApplication={editApplication} />
    </Box>
  );
}
