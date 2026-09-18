import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import AddIcon from '@mui/icons-material/Add';
import { useDocuments, useDeleteDocument, useReviewDocument } from '../hooks/useDocuments';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import DocumentFormDialog from '../components/DocumentFormDialog';
import type { Document } from '../api/documentsApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  uploaded: 'info',
  under_review: 'warning',
  verified: 'success',
  rejected: 'error',
  expired: 'default',
  superseded: 'default',
};

export default function Documents() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const { data, isLoading, isError, refetch } = useDocuments({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteDocument();
  const { mutate: review } = useReviewDocument();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((d) => d.fileName?.toLowerCase().includes(term) || d.documentType?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<Document>[] = useMemo(() => [
    { field: 'fileName', headerName: 'File name', width: 220 },
    { field: 'documentType', headerName: 'Type', width: 160, renderCell: (row) => <Chip label={row.documentType?.replace(/_/g, ' ')} size="small" /> },
    { field: 'category', headerName: 'Category', width: 120 },
    { field: 'status', headerName: 'Status', width: 140, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'uploaded']} /> },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'uploaded' || row.status === 'under_review' ? [
              { label: 'Verify', icon: <CheckCircleIcon fontSize="small" color="success" />, onClick: () => review({ id: row.id!, decision: 'verified' }) },
              { label: 'Reject', icon: <CancelIcon fontSize="small" color="error" />, color: 'error' as const, onClick: () => review({ id: row.id!, decision: 'rejected' }) },
            ] : []),
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete document',
                message: `Delete document "${row.fileName}"?`,
                confirmLabel: 'Delete', severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [confirm, remove, review]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Documents"
        description="Manage document metadata and review status. File bytes live in external object storage."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Register Document</Button>}
      />
      <ServerDataTable
        title="Documents"
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
      <DocumentFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </Box>
  );
}
