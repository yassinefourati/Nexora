import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useTags, useDeleteTag } from '../hooks/useTags';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import TagFormDialog from '../components/TagFormDialog';
import type { Tag } from '../api/tagsApi';

export default function Tags() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editTag, setEditTag] = useState<Tag | null>(null);

  const { data, isLoading, isError, refetch } = useTags({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteTag();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((t) => t.name?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<Tag>[] = useMemo(() => [
    { field: 'name', headerName: 'Name', width: 200, renderCell: (row) => <Chip label={row.name} size="small" sx={row.color ? { bgcolor: row.color, color: '#fff' } : undefined} /> },
    { field: 'color', headerName: 'Color', width: 120 },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditTag(row); setDialogOpen(true); } },
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete tag', message: `Delete tag "${row.name}"?`, confirmLabel: 'Delete', severity: 'error',
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
        title="Tags"
        description="Labels that can be attached to any entity."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditTag(null); setDialogOpen(true); }}>Add Tag</Button>}
      />
      <ServerDataTable
        title="Tags"
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
      <TagFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditTag(null); }} editTag={editTag} />
    </Box>
  );
}
