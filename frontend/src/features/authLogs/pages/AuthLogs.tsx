import { useState, useMemo } from 'react';
import { Box, Chip } from '@mui/material';
import { useAuthLogs } from '../hooks/useAuthLogs';
import PageHeader from '@/shared/components/PageHeader';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import type { AuthLog } from '../api/authLogsApi';

export default function AuthLogs() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>({ field: 'createdAt', direction: 'desc' });
  const [search, setSearch] = useState('');

  const { data, isLoading, isError, refetch } = useAuthLogs({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((e) => e.eventType?.toLowerCase().includes(term) || e.ipAddress?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<AuthLog>[] = useMemo(() => [
    { field: 'eventType', headerName: 'Event', width: 160, renderCell: (row) => <Chip label={row.eventType} size="small" variant="outlined" /> },
    { field: 'ipAddress', headerName: 'IP', width: 140 },
    { field: 'userAgent', headerName: 'User agent', width: 260 },
    { field: 'createdAt', headerName: 'Time', width: 180, renderCell: (row) => row.createdAt ? new Date(row.createdAt).toLocaleString() : '—' },
  ], []);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader title="Auth Logs" description="Authentication events — logins, logouts, token refreshes." />
      <ServerDataTable
        title="Auth Logs"
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
