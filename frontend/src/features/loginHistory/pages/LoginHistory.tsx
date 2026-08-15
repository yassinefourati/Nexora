import { useState, useMemo } from 'react';
import { Box, Chip } from '@mui/material';
import { useLoginHistory } from '../hooks/useLoginHistory';
import PageHeader from '@/shared/components/PageHeader';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import type { LoginHistory } from '../api/loginHistoryApi';

export default function LoginHistoryPage() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>({ field: 'loginAt', direction: 'desc' });
  const [search, setSearch] = useState('');

  const { data, isLoading, isError, refetch } = useLoginHistory({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((e) => e.ipAddress?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<LoginHistory>[] = useMemo(() => [
    { field: 'success', headerName: 'Result', width: 100, renderCell: (row) => <Chip label={row.success ? 'Success' : 'Failed'} size="small" color={row.success ? 'success' : 'error'} /> },
    { field: 'ipAddress', headerName: 'IP', width: 140 },
    { field: 'userAgent', headerName: 'User agent', width: 240 },
    { field: 'loginAt', headerName: 'Login at', width: 180, renderCell: (row) => row.loginAt ? new Date(row.loginAt).toLocaleString() : '—' },
    { field: 'logoutAt', headerName: 'Logout at', width: 180, renderCell: (row) => row.logoutAt ? new Date(row.logoutAt).toLocaleString() : '—' },
  ], []);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader title="Login History" description="Historical record of login attempts and session duration." />
      <ServerDataTable
        title="Login History"
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
