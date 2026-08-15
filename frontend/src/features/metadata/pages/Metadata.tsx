import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useMetadata, useDeleteMetadata } from '../hooks/useMetadata';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import MetadataFormDialog from '../components/MetadataFormDialog';
import type { MetadataKv } from '../api/metadataApi';

export default function Metadata() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editEntry, setEditEntry] = useState<MetadataKv | null>(null);

  const { data, isLoading, isError, refetch } = useMetadata({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteMetadata();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((m) => m.key?.toLowerCase().includes(term) || m.entityType?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<MetadataKv>[] = useMemo(() => [
    { field: 'entityType', headerName: 'Entity type', width: 140, renderCell: (row) => <Chip label={row.entityType} size="small" variant="outlined" /> },
    { field: 'entityId', headerName: 'Entity ID', width: 220 },
    { field: 'key', headerName: 'Key', width: 160 },
    { field: 'value', headerName: 'Value', width: 220 },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditEntry(row); setDialogOpen(true); } },
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete metadata entry', message: `Delete metadata key "${row.key}"?`, confirmLabel: 'Delete', severity: 'error',
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
        title="Metadata"
        description="Free-form key/value metadata attached to arbitrary entities."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditEntry(null); setDialogOpen(true); }}>Add Metadata</Button>}
      />
      <ServerDataTable
        title="Metadata"
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
      <MetadataFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditEntry(null); }} editEntry={editEntry} />
    </Box>
  );
}
