import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useCustomers, useDeleteCustomer } from '../hooks/useCustomers';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import CustomerFormDialog from '../components/CustomerFormDialog';
import type { Customer } from '../api/customersApi';

function displayName(customer: Customer): string {
  if (customer.customerType === 'business') return customer.businessName ?? '';
  return [customer.firstName, customer.lastName].filter(Boolean).join(' ');
}

export default function Customers() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editCustomer, setEditCustomer] = useState<Customer | null>(null);

  const { data, isLoading, isError, refetch } = useCustomers({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteCustomer();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((c) => displayName(c).toLowerCase().includes(term) || c.email?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<Customer>[] = useMemo(() => [
    { field: 'customerType', headerName: 'Type', width: 100, renderCell: (row) => <Chip label={row.customerType} size="small" /> },
    { field: 'firstName', headerName: 'Name', width: 200, renderCell: (row) => displayName(row) },
    { field: 'email', headerName: 'Email', width: 220 },
    { field: 'status', headerName: 'Status', width: 120, renderCell: (row) => <Chip label={row.status} size="small" color={row.status === 'active' ? 'success' : 'default'} /> },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditCustomer(row); setDialogOpen(true); } },
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete customer',
                message: `Delete customer "${displayName(row)}"?`,
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
        title="Customers"
        description="Manage loan platform customers (individual or business applicants)."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditCustomer(null); setDialogOpen(true); }}>Add Customer</Button>}
      />
      <ServerDataTable
        title="Customers"
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
      <CustomerFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditCustomer(null); }} editCustomer={editCustomer} />
    </Box>
  );
}
