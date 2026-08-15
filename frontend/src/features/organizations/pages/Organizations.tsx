import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useOrganizations, useDeleteOrganization } from '../hooks/useOrganizations';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import OrganizationFormDialog from '../components/OrganizationFormDialog';
import type { Organization } from '../api/organizationsApi';

export default function Organizations() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editOrg, setEditOrg] = useState<Organization | null>(null);

  const { data, isLoading, isError, refetch } = useOrganizations({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteOrganization();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((o) => o.name?.toLowerCase().includes(term) || o.code?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<Organization>[] = useMemo(() => [
    { field: 'name', headerName: 'Name', width: 200 },
    { field: 'code', headerName: 'Code', width: 120 },
    { field: 'status', headerName: 'Status', width: 120, renderCell: (row) => <Chip label={row.status} size="small" color={row.status === 'active' ? 'success' : 'default'} /> },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditOrg(row); setDialogOpen(true); } },
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete organization',
                message: `Delete organization "${row.name}"?`,
                confirmLabel: 'Delete', severity: 'error',
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
        title="Organizations"
        description="Manage organizations and their hierarchy."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditOrg(null); setDialogOpen(true); }}>Add Organization</Button>}
      />
      <ServerDataTable
        title="Organizations"
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
      <OrganizationFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditOrg(null); }} editOrg={editOrg} />
    </Box>
  );
}
