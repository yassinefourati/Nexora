import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useLoanProducts, useDeleteLoanProduct } from '../hooks/useLoanProducts';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import LoanProductFormDialog from '../components/LoanProductFormDialog';
import type { LoanProduct } from '../api/loanProductsApi';

export default function LoanProducts() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editProduct, setEditProduct] = useState<LoanProduct | null>(null);

  const { data, isLoading, isError, refetch } = useLoanProducts({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteLoanProduct();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((p) => p.name?.toLowerCase().includes(term) || p.code?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<LoanProduct>[] = useMemo(() => [
    { field: 'code', headerName: 'Code', width: 120 },
    { field: 'name', headerName: 'Name', width: 200 },
    { field: 'productType', headerName: 'Type', width: 130, renderCell: (row) => <Chip label={row.productType} size="small" /> },
    { field: 'minAmount', headerName: 'Amount range', width: 180, sortable: false, renderCell: (row) => `${row.currency} ${row.minAmount} - ${row.maxAmount}` },
    { field: 'status', headerName: 'Status', width: 120, renderCell: (row) => <Chip label={row.status} size="small" color={row.status === 'active' ? 'success' : 'default'} /> },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditProduct(row); setDialogOpen(true); } },
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete loan product',
                message: `Delete loan product "${row.name}"?`,
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
        title="Loan Products"
        description="Manage the configurable catalog of loan products offered by the platform."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditProduct(null); setDialogOpen(true); }}>Add Loan Product</Button>}
      />
      <ServerDataTable
        title="Loan Products"
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
      <LoanProductFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditProduct(null); }} editProduct={editProduct} />
    </Box>
  );
}
