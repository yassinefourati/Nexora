import { useState, useMemo } from 'react';
import { Box, Chip, Button } from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import { useSessionsList, useRevokeSession } from '../hooks/useSessions';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import type { Session } from '../api/sessionsApi';

export default function Sessions() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>({ field: 'createdAt', direction: 'desc' });
  const [search, setSearch] = useState('');

  const { data, isLoading, isError, refetch } = useSessionsList({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: revoke } = useRevokeSession();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((s) => s.ipAddress?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<Session>[] = useMemo(() => [
    { field: 'ipAddress', headerName: 'IP', width: 140 },
    { field: 'userAgent', headerName: 'User agent', width: 240 },
    {
      field: 'revokedAt', headerName: 'Status', width: 110,
      renderCell: (row) => <Chip label={row.revokedAt ? 'Revoked' : 'Active'} size="small" color={row.revokedAt ? 'default' : 'success'} />,
    },
    { field: 'expiresAt', headerName: 'Expires', width: 180, renderCell: (row) => row.expiresAt ? new Date(row.expiresAt).toLocaleString() : '—' },
    {
      field: 'actions', headerName: '', width: 110, sortable: false,
      renderCell: (row) => row.revokedAt ? null : (
        <Button size="small" color="error" startIcon={<BlockIcon fontSize="small" />} onClick={() => confirm({
          title: 'Revoke session', message: 'Revoke this session? The user will be signed out immediately.', confirmLabel: 'Revoke', severity: 'error',
          onConfirm: () => revoke(row.id!),
        })}>
          Revoke
        </Button>
      ),
    },
  ], [confirm, revoke]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader title="Sessions" description="Active and revoked user sessions." />
      <ServerDataTable
        title="Sessions"
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
    </Box>
  );
}
