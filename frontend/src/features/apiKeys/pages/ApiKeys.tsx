import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import AddIcon from '@mui/icons-material/Add';
import { useApiKeysList, useRevokeApiKey } from '../hooks/useApiKeys';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import ApiKeyFormDialog from '../components/ApiKeyFormDialog';
import type { ApiKey } from '../api/apiKeysApi';

export default function ApiKeys() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>({ field: 'createdAt', direction: 'desc' });
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const { data, isLoading, isError, refetch } = useApiKeysList({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: revoke } = useRevokeApiKey();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((k) => k.name?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<ApiKey>[] = useMemo(() => [
    { field: 'name', headerName: 'Name', width: 180 },
    { field: 'scopes', headerName: 'Scopes', width: 220, renderCell: (row) => (row.scopes ?? []).map((s) => <Chip key={s} label={s} size="small" sx={{ mr: 0.5 }} />) },
    {
      field: 'revokedAt', headerName: 'Status', width: 110,
      renderCell: (row) => <Chip label={row.revokedAt ? 'Revoked' : 'Active'} size="small" color={row.revokedAt ? 'default' : 'success'} />,
    },
    { field: 'lastUsedAt', headerName: 'Last used', width: 180, renderCell: (row) => row.lastUsedAt ? new Date(row.lastUsedAt).toLocaleString() : 'Never' },
    { field: 'expiresAt', headerName: 'Expires', width: 180, renderCell: (row) => row.expiresAt ? new Date(row.expiresAt).toLocaleString() : 'Never' },
    {
      field: 'actions', headerName: '', width: 110, sortable: false,
      renderCell: (row) => row.revokedAt ? null : (
        <Button size="small" color="error" startIcon={<BlockIcon fontSize="small" />} onClick={() => confirm({
          title: 'Revoke API key', message: `Revoke API key "${row.name}"? Requests using it will be rejected immediately.`, confirmLabel: 'Revoke', severity: 'error',
          onConfirm: () => revoke(row.id!),
        })}>
          Revoke
        </Button>
      ),
    },
  ], [confirm, revoke]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="API Keys"
        description="Programmatic access keys issued to users."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Create API Key</Button>}
      />
      <ServerDataTable
        title="API Keys"
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
      <ApiKeyFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </Box>
  );
}
