import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import { useLoanOffers, useDeleteLoanOffer, useAcceptLoanOffer } from '../hooks/useLoanOffers';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import LoanOfferFormDialog from '../components/LoanOfferFormDialog';
import DeclineLoanOfferDialog from '../components/DeclineLoanOfferDialog';
import type { LoanOffer } from '../api/loanOffersApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  issued: 'info',
  accepted: 'success',
  declined: 'error',
  expired: 'warning',
};

export default function LoanOffers() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [declineTarget, setDeclineTarget] = useState<LoanOffer | null>(null);

  const { data, isLoading, isError, refetch } = useLoanOffers({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteLoanOffer();
  const { mutate: accept } = useAcceptLoanOffer();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((o) => o.status?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<LoanOffer>[] = useMemo(() => [
    { field: 'status', headerName: 'Status', width: 130, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'issued']} /> },
    { field: 'offeredAmount', headerName: 'Offered amount', width: 150 },
    { field: 'offeredTermMonths', headerName: 'Term (mo)', width: 110 },
    { field: 'interestRate', headerName: 'Rate (%)', width: 110 },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'issued' ? [
              { label: 'Accept', icon: <CheckCircleIcon fontSize="small" />, onClick: () => accept(row.id!) },
              { label: 'Decline', icon: <CancelIcon fontSize="small" color="error" />, color: 'error' as const, onClick: () => setDeclineTarget(row) },
            ] : []),
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete loan offer',
                message: 'Delete this loan offer?',
                confirmLabel: 'Delete', severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [confirm, remove, accept]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Loan Offers"
        description="Present approved loan terms to the customer for acceptance or decline."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Issue Loan Offer</Button>}
      />
      <ServerDataTable
        title="Loan Offers"
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
      <LoanOfferFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
      <DeclineLoanOfferDialog open={!!declineTarget} onClose={() => setDeclineTarget(null)} loanOffer={declineTarget} />
    </Box>
  );
}
