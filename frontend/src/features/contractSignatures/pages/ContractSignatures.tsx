import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import { useContractSignatures, useSignContractSignature } from '../hooks/useContractSignatures';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import ContractSignatureFormDialog from '../components/ContractSignatureFormDialog';
import DeclineContractSignatureDialog from '../components/DeclineContractSignatureDialog';
import type { ContractSignature } from '../api/contractSignaturesApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  pending: 'warning',
  signed: 'success',
  declined: 'error',
};

export default function ContractSignatures() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [declineTarget, setDeclineTarget] = useState<ContractSignature | null>(null);

  const { data, isLoading, isError, refetch } = useContractSignatures({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: sign } = useSignContractSignature();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((s) => s.signerName?.toLowerCase().includes(term) || s.status?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<ContractSignature>[] = useMemo(() => [
    { field: 'signerName', headerName: 'Signer', width: 180 },
    { field: 'signerRole', headerName: 'Role', width: 160, renderCell: (row) => row.signerRole?.replace(/_/g, ' ') },
    { field: 'status', headerName: 'Status', width: 130, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'pending']} /> },
    { field: 'signatureMethod', headerName: 'Method', width: 130 },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'pending' ? [
              { label: 'Sign', icon: <CheckCircleIcon fontSize="small" />, onClick: () => sign(row.id!) },
              { label: 'Decline', icon: <CancelIcon fontSize="small" color="error" />, color: 'error' as const, onClick: () => setDeclineTarget(row) },
            ] : []),
          ]}
        />
      ),
    },
  ], [sign]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Contract Signatures"
        description="Request and capture signer signatures on a finalized loan contract."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Request Signature</Button>}
      />
      <ServerDataTable
        title="Contract Signatures"
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
      <ContractSignatureFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
      <DeclineContractSignatureDialog open={!!declineTarget} onClose={() => setDeclineTarget(null)} contractSignature={declineTarget} />
    </Box>
  );
}
