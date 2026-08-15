import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useNotificationTemplates, useDeleteNotificationTemplate } from '../hooks/useNotificationTemplates';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import NotificationTemplateFormDialog from '../components/NotificationTemplateFormDialog';
import type { NotificationTemplate } from '../api/notificationTemplatesApi';

export default function NotificationTemplates() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editTemplate, setEditTemplate] = useState<NotificationTemplate | null>(null);

  const { data, isLoading, isError, refetch } = useNotificationTemplates({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteNotificationTemplate();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((t) => t.name?.toLowerCase().includes(term) || t.code?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<NotificationTemplate>[] = useMemo(() => [
    { field: 'code', headerName: 'Code', width: 160 },
    { field: 'name', headerName: 'Name', width: 200 },
    { field: 'channel', headerName: 'Channel', width: 110, renderCell: (row) => <Chip label={row.channel} size="small" variant="outlined" /> },
    { field: 'subjectTemplate', headerName: 'Subject', width: 220 },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditTemplate(row); setDialogOpen(true); } },
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete notification template', message: `Delete template "${row.name}"?`, confirmLabel: 'Delete', severity: 'error',
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
        title="Notification Templates"
        description="Reusable templates for outgoing notifications."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditTemplate(null); setDialogOpen(true); }}>Add Template</Button>}
      />
      <ServerDataTable
        title="Notification Templates"
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
      <NotificationTemplateFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditTemplate(null); }} editTemplate={editTemplate} />
    </Box>
  );
}
