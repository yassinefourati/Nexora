import { useMemo, useState } from 'react';
import { Box, Typography, Chip, Tooltip, MenuItem, ListItemIcon, Button, Stack, Link } from '@mui/material';
import {
  MaterialReactTable, useMaterialReactTable,
  type MRT_ColumnDef, type MRT_Row,
} from 'material-react-table';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import TableRowsIcon from '@mui/icons-material/TableRows';
import { generateEmployees, type Employee } from '../data/employees';
import { exportCsv, exportPdf } from '@/shared/components/AdvancedDataTable/exportUtils';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';

const statusColor: Record<Employee['status'], 'success' | 'warning' | 'default'> = {
  Active: 'success', 'On Leave': 'warning', Terminated: 'default',
};

const currency = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 });

const exportColumns: ColumnDef<Employee>[] = [
  { field: 'firstName', headerName: 'First name' },
  { field: 'lastName', headerName: 'Last name' },
  { field: 'email', headerName: 'Email' },
  { field: 'department', headerName: 'Department' },
  { field: 'role', headerName: 'Role' },
  { field: 'salary', headerName: 'Salary', exportValue: (r) => currency.format(r.salary) },
  { field: 'startDate', headerName: 'Start date' },
  { field: 'status', headerName: 'Status' },
  { field: 'location', headerName: 'Location' },
];

export default function AdvancedTableDemo() {
  const [data] = useState<Employee[]>(() => generateEmployees(200));

  const columns = useMemo<MRT_ColumnDef<Employee>[]>(() => [
    {
      header: 'Name',
      id: 'name',
      accessorFn: (row) => `${row.firstName} ${row.lastName}`,
      filterVariant: 'text',
      size: 180,
    },
    { accessorKey: 'email', header: 'Email', size: 220 },
    {
      accessorKey: 'department', header: 'Department', filterVariant: 'select',
      filterSelectOptions: ['Engineering', 'Sales', 'Marketing', 'Support', 'HR', 'Finance'],
      enableGrouping: true, size: 150,
    },
    { accessorKey: 'role', header: 'Role', size: 190 },
    {
      accessorKey: 'salary', header: 'Salary', filterVariant: 'range', size: 130,
      Cell: ({ cell }) => currency.format(cell.getValue<number>()),
      aggregationFn: 'mean',
      AggregatedCell: ({ cell }) => <>Avg: {currency.format(Math.round(cell.getValue<number>()))}</>,
    },
    {
      accessorKey: 'startDate', header: 'Start date', filterVariant: 'date', size: 130,
    },
    {
      accessorKey: 'status', header: 'Status', filterVariant: 'select',
      filterSelectOptions: ['Active', 'On Leave', 'Terminated'], size: 120,
      Cell: ({ cell }) => {
        const v = cell.getValue<Employee['status']>();
        return <Chip label={v} size="small" color={statusColor[v]} variant={v === 'Terminated' ? 'outlined' : 'filled'} />;
      },
    },
    {
      accessorKey: 'location', header: 'Location', filterVariant: 'select',
      filterSelectOptions: ['Montreal', 'Toronto', 'New York', 'Austin', 'London', 'Berlin', 'Remote'],
      enableGrouping: true, size: 130,
    },
  ], []);

  const table = useMaterialReactTable({
    columns,
    data,
    enableColumnFilterModes: true,
    enableColumnOrdering: true,
    enableGrouping: true,
    enableColumnPinning: true,
    enableFacetedValues: true,
    enableRowSelection: true,
    enableRowActions: true,
    enableStickyHeader: true,
    enableDensityToggle: true,
    columnFilterDisplayMode: 'popover',
    paginationDisplayMode: 'pages',
    positionToolbarAlertBanner: 'bottom',
    muiTableContainerProps: { sx: { maxHeight: 560 } },
    initialState: {
      showColumnFilters: false,
      density: 'compact',
      pagination: { pageIndex: 0, pageSize: 10 },
      columnPinning: { left: ['mrt-row-select', 'name'] },
    },
    muiPaginationProps: { rowsPerPageOptions: [10, 20, 50], shape: 'rounded' },
    renderRowActionMenuItems: ({ closeMenu }) => [
      <MenuItem key="edit" onClick={closeMenu}>
        <ListItemIcon><EditIcon fontSize="small" /></ListItemIcon> Edit
      </MenuItem>,
      <MenuItem key="delete" onClick={closeMenu} sx={{ color: 'error.main' }}>
        <ListItemIcon><DeleteIcon fontSize="small" color="error" /></ListItemIcon> Delete
      </MenuItem>,
    ],
    renderTopToolbarCustomActions: ({ table: t }) => {
      const selected = t.getSelectedRowModel().rows;
      const rowsToExport = (selected.length ? selected : t.getPrePaginationRowModel().rows).map((r: MRT_Row<Employee>) => r.original);
      return (
        <Stack direction="row" spacing={1} sx={{ py: 0.5 }}>
          <Tooltip title={selected.length ? `Export ${selected.length} selected as CSV` : 'Export all filtered rows as CSV'}>
            <Button size="small" variant="outlined" startIcon={<TableRowsIcon fontSize="small" />} onClick={() => exportCsv(rowsToExport, exportColumns, 'employees-export')}>
              CSV
            </Button>
          </Tooltip>
          <Tooltip title={selected.length ? `Export ${selected.length} selected as PDF` : 'Export all filtered rows as PDF'}>
            <Button size="small" variant="outlined" startIcon={<PictureAsPdfIcon fontSize="small" />} onClick={() => exportPdf(rowsToExport, exportColumns, 'Employees', 'employees-export')}>
              PDF
            </Button>
          </Tooltip>
        </Stack>
      );
    },
  });

  return (
    <Box>
      <Box sx={{ mb: 2 }}>
        <Typography variant="h4" fontWeight={700}>Advanced Table Demo</Typography>
        <Typography variant="body2" color="text.secondary">
          Built with{' '}
          <Link href="https://www.material-react-table.com/docs/examples/advanced" target="_blank" rel="noreferrer">material-react-table</Link>
          {' '}— global &amp; column search, filters, sorting, grouping, column pinning/ordering, density toggle, row selection, and CSV/PDF export. 200 mock rows.
        </Typography>
      </Box>
      <MaterialReactTable table={table} />
    </Box>
  );
}
