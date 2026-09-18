import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import BlockIcon from '@mui/icons-material/Block';
import AddIcon from '@mui/icons-material/Add';
import { useCollectionCases, useDeleteCollectionCase } from '../hooks/useCollectionCases';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import CollectionCaseFormDialog from '../components/CollectionCaseFormDialog';
import EscalateCollectionCaseDialog from '../components/EscalateCollectionCaseDialog';
import ResolveCollectionCaseDialog from '../components/ResolveCollectionCaseDialog';
import WriteOffCollectionCaseDialog from '../components/WriteOffCollectionCaseDialog';
import type { CollectionCase } from '../api/collectionCasesApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  open: 'warning',
  in_progress: 'info',
  resolved: 'success',
  written_off: 'error',
};

export default function CollectionCases() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [escalateTarget, setEscalateTarget] = useState<CollectionCase | null>(null);
  const [resolveTarget, setResolveTarget] = useState<CollectionCase | null>(null);
  const [writeOffTarget, setWriteOffTarget] = useState<CollectionCase | null>(null);

  const { data, isLoading, isError, refetch } = useCollectionCases({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteCollectionCase();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((c) => c.status?.toLowerCase().includes(term) || c.assignedTo?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<CollectionCase>[] = useMemo(() => [
    { field: 'status', headerName: 'Status', width: 130, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'open']} /> },
    { field: 'stage', headerName: 'Stage', width: 140, renderCell: (row) => row.stage?.replace(/_/g, ' ') },
    { field: 'overdueAmount', headerName: 'Overdue amount', width: 150 },
    { field: 'assignedTo', headerName: 'Assigned to', width: 160 },
    {
      field: 'actions', headerName: '', width: 130, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status !== 'resolved' && row.status !== 'written_off' ? [
              { label: 'Escalate', icon: <TrendingUpIcon fontSize="small" />, onClick: () => setEscalateTarget(row) },
              { label: 'Resolve', icon: <CheckCircleIcon fontSize="small" />, onClick: () => setResolveTarget(row) },
              { label: 'Write off', icon: <BlockIcon fontSize="small" color="error" />, color: 'error' as const, onClick: () => setWriteOffTarget(row) },
            ] : []),
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete collection case',
                message: 'Delete this collection case?',
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
        title="Collection Cases"
        description="Track collection efforts on overdue loan installments through to resolution or write-off."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Open Collection Case</Button>}
      />
      <ServerDataTable
        title="Collection Cases"
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
      <CollectionCaseFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
      <EscalateCollectionCaseDialog open={!!escalateTarget} onClose={() => setEscalateTarget(null)} collectionCase={escalateTarget} />
      <ResolveCollectionCaseDialog open={!!resolveTarget} onClose={() => setResolveTarget(null)} collectionCase={resolveTarget} />
      <WriteOffCollectionCaseDialog open={!!writeOffTarget} onClose={() => setWriteOffTarget(null)} collectionCase={writeOffTarget} />
    </Box>
  );
}
