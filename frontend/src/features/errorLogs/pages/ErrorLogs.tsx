import { useState, useMemo } from 'react';
import { Box, Chip } from '@mui/material';
import { useErrorLogs } from '../hooks/useErrorLogs';
import PageHeader from '@/shared/components/PageHeader';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import type { ErrorLog } from '../api/errorLogsApi';

const SEVERITY_COLOR: Record<string, 'error' | 'warning' | 'default'> = { critical: 'error', error: 'error', warning: 'warning', info: 'default' };

export default function ErrorLogs() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>({ field: 'createdAt', direction: 'desc' });
  const [search, setSearch] = useState('');

  const { data, isLoading, isError, refetch } = useErrorLogs({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((e) => e.errorMessage?.toLowerCase().includes(term) || e.source?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<ErrorLog>[] = useMemo(() => [
    { field: 'severity', headerName: 'Severity', width: 110, renderCell: (row) => <Chip label={row.severity} size="small" color={SEVERITY_COLOR[row.severity?.toLowerCase() ?? ''] ?? 'default'} /> },
    { field: 'source', headerName: 'Source', width: 160 },
    { field: 'errorMessage', headerName: 'Message', width: 320 },
    { field: 'createdAt', headerName: 'Time', width: 180, renderCell: (row) => row.createdAt ? new Date(row.createdAt).toLocaleString() : '—' },
  ], []);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader title="Error Logs" description="Application errors captured by the backend." />
      <ServerDataTable
        title="Error Logs"
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
