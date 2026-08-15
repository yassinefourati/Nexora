import { useState, useMemo } from 'react';
import { Box, Button } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useDepartments, useDeleteDepartment } from '../hooks/useDepartments';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import DepartmentFormDialog from '../components/DepartmentFormDialog';
import type { Department } from '../api/departmentsApi';

export default function Departments() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editDept, setEditDept] = useState<Department | null>(null);

  const { data, isLoading, isError, refetch } = useDepartments({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteDepartment();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((d) => d.name?.toLowerCase().includes(term) || d.code?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<Department>[] = useMemo(() => [
    { field: 'name', headerName: 'Name', width: 200 },
    { field: 'code', headerName: 'Code', width: 120 },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditDept(row); setDialogOpen(true); } },
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete department',
                message: `Delete department "${row.name}"?`,
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
        title="Departments"
        description="Manage departments within organizations."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditDept(null); setDialogOpen(true); }}>Add Department</Button>}
      />
      <ServerDataTable
        title="Departments"
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
      <DepartmentFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditDept(null); }} editDept={editDept} />
    </Box>
  );
}
