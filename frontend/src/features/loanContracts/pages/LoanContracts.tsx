import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import { useLoanContracts, useDeleteLoanContract } from '../hooks/useLoanContracts';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import LoanContractFormDialog from '../components/LoanContractFormDialog';
import FinalizeLoanContractDialog from '../components/FinalizeLoanContractDialog';
import CancelLoanContractDialog from '../components/CancelLoanContractDialog';
import type { LoanContract } from '../api/loanContractsApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  draft: 'default',
  finalized: 'success',
  cancelled: 'error',
};

export default function LoanContracts() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [finalizeTarget, setFinalizeTarget] = useState<LoanContract | null>(null);
  const [cancelTarget, setCancelTarget] = useState<LoanContract | null>(null);

  const { data, isLoading, isError, refetch } = useLoanContracts({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteLoanContract();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((c) => c.status?.toLowerCase().includes(term) || c.contractNumber?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<LoanContract>[] = useMemo(() => [
    { field: 'contractNumber', headerName: 'Contract #', width: 150 },
    { field: 'status', headerName: 'Status', width: 130, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'draft']} /> },
    { field: 'principalAmount', headerName: 'Principal', width: 140 },
    { field: 'termMonths', headerName: 'Term (mo)', width: 110 },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'draft' ? [
              { label: 'Finalize', icon: <CheckCircleIcon fontSize="small" />, onClick: () => setFinalizeTarget(row) },
              { label: 'Cancel', icon: <CancelIcon fontSize="small" color="error" />, color: 'error' as const, onClick: () => setCancelTarget(row) },
            ] : []),
            ...(row.status === 'finalized' ? [
              { label: 'Cancel', icon: <CancelIcon fontSize="small" color="error" />, color: 'error' as const, onClick: () => setCancelTarget(row) },
            ] : []),
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete loan contract',
                message: 'Delete this loan contract?',
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
        title="Loan Contracts"
        description="Generate and manage the contract document for an accepted loan offer."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Generate Contract</Button>}
      />
      <ServerDataTable
        title="Loan Contracts"
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
      <LoanContractFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
      <FinalizeLoanContractDialog open={!!finalizeTarget} onClose={() => setFinalizeTarget(null)} loanContract={finalizeTarget} />
      <CancelLoanContractDialog open={!!cancelTarget} onClose={() => setCancelTarget(null)} loanContract={cancelTarget} />
    </Box>
  );
}
