import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { useEntityTags, useDeleteEntityTag } from '../hooks/useEntityTags';
import { useTags } from '@/features/tags/hooks/useTags';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import EntityTagFormDialog from '../components/EntityTagFormDialog';
import type { EntityTag } from '../api/entityTagsApi';

export default function EntityTags() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const { data, isLoading, isError, refetch } = useEntityTags({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { data: tagsPage } = useTags({ page: 0, size: 100 });
  const { mutate: remove } = useDeleteEntityTag();
  const { confirm } = useConfirmStore();

  const tagName = useMemo(() => {
    const map = new Map((tagsPage?.items ?? []).map((t) => [t.id, t.name]));
    return (id?: string) => (id && map.get(id)) || id || '—';
  }, [tagsPage]);

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((et) => et.entityType?.toLowerCase().includes(term) || tagName(et.tagId).toLowerCase().includes(term));
  }, [data, search, tagName]);

  const columns: ColumnDef<EntityTag>[] = useMemo(() => [
    { field: 'tagId', headerName: 'Tag', width: 160, renderCell: (row) => <Chip label={tagName(row.tagId)} size="small" /> },
    { field: 'entityType', headerName: 'Entity type', width: 160 },
    { field: 'entityId', headerName: 'Entity ID', width: 220 },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            {
              label: 'Detach', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Detach tag', message: 'Remove this tag from the entity?', confirmLabel: 'Detach', severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [confirm, remove, tagName]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Entity Tags"
        description="Tags attached to specific entities across the system."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Attach Tag</Button>}
      />
      <ServerDataTable
        title="Entity Tags"
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
      <EntityTagFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </Box>
  );
}
