import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import { useLoanApprovals, useDeleteLoanApproval } from '../hooks/useLoanApprovals';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import LoanApprovalFormDialog from '../components/LoanApprovalFormDialog';
import ApproveLoanApprovalDialog from '../components/ApproveLoanApprovalDialog';
import RejectLoanApprovalDialog from '../components/RejectLoanApprovalDialog';
import type { LoanApproval } from '../api/loanApprovalsApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  pending: 'default',
  approved: 'success',
  rejected: 'error',
  expired: 'warning',
};

export default function LoanApprovals() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [approveTarget, setApproveTarget] = useState<LoanApproval | null>(null);
  const [rejectTarget, setRejectTarget] = useState<LoanApproval | null>(null);

  const { data, isLoading, isError, refetch } = useLoanApprovals({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteLoanApproval();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((a) => a.status?.toLowerCase().includes(term) || a.approvedBy?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<LoanApproval>[] = useMemo(() => [
    { field: 'status', headerName: 'Status', width: 130, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'pending']} /> },
    { field: 'approvedAmount', headerName: 'Approved amount', width: 150 },
    { field: 'interestRate', headerName: 'Rate (%)', width: 110 },
    { field: 'approvedBy', headerName: 'Approved by', width: 160 },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'pending' ? [
              { label: 'Approve', icon: <CheckCircleIcon fontSize="small" />, onClick: () => setApproveTarget(row) },
              { label: 'Reject', icon: <CancelIcon fontSize="small" color="error" />, color: 'error' as const, onClick: () => setRejectTarget(row) },
            ] : []),
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete loan approval',
                message: 'Delete this loan approval?',
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
        title="Loan Approvals"
        description="Formalize completed underwriting decisions into approved loan terms."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Open Loan Approval</Button>}
      />
      <ServerDataTable
        title="Loan Approvals"
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
      <LoanApprovalFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
      <ApproveLoanApprovalDialog open={!!approveTarget} onClose={() => setApproveTarget(null)} loanApproval={approveTarget} />
      <RejectLoanApprovalDialog open={!!rejectTarget} onClose={() => setRejectTarget(null)} loanApproval={rejectTarget} />
    </Box>
  );
}
