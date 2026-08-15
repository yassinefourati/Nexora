import { useState, useMemo } from 'react';
import { Box, Chip } from '@mui/material';
import { useAuditLog } from '../hooks/useAudit';
import PageHeader from '@/shared/components/PageHeader';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import type { AuditLog } from '../api/auditApi';

export default function AuditLogPage() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>({ field: 'createdAt', direction: 'desc' });
  const [search, setSearch] = useState('');

  const { data, isLoading, isError, refetch } = useAuditLog({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((e) => e.action?.toLowerCase().includes(term) || e.entityType?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<AuditLog>[] = useMemo(() => [
    { field: 'action', headerName: 'Action', width: 160 },
    { field: 'entityType', headerName: 'Entity', width: 140, renderCell: (row) => <Chip label={row.entityType} size="small" variant="outlined" /> },
    { field: 'entityId', headerName: 'Entity ID', width: 220 },
    { field: 'ipAddress', headerName: 'IP', width: 130 },
    { field: 'createdAt', headerName: 'Time', width: 180, renderCell: (row) => row.createdAt ? new Date(row.createdAt).toLocaleString() : '—' },
  ], []);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader title="Audit Log" description="Record of all data-changing actions across the system." />
      <ServerDataTable
        title="Audit Log"
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
