import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useLoanNotifications } from '../hooks/useLoanNotifications';
import PageHeader from '@/shared/components/PageHeader';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import LoanNotificationFormDialog from '../components/LoanNotificationFormDialog';
import type { LoanNotification } from '../api/loanNotificationsApi';

const CHANNEL_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  email: 'info',
  sms: 'warning',
  in_app: 'default',
};

export default function LoanNotifications() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const { data, isLoading, isError, refetch } = useLoanNotifications({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((n) => n.title?.toLowerCase().includes(term) || n.eventType?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<LoanNotification>[] = useMemo(() => [
    { field: 'eventType', headerName: 'Event', width: 200, renderCell: (row) => row.eventType?.replace(/_/g, ' ') },
    { field: 'title', headerName: 'Title', width: 220 },
    { field: 'channel', headerName: 'Channel', width: 130, renderCell: (row) => <Chip label={row.channel} size="small" color={CHANNEL_COLOR[row.channel ?? 'email']} /> },
    { field: 'createdAt', headerName: 'Sent at', width: 200 },
  ], []);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Loan Notifications"
        description="Notifications sent about a loan application's lifecycle events."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Send Notification</Button>}
      />
      <ServerDataTable
        title="Loan Notifications"
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
      <LoanNotificationFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </Box>
  );
}
