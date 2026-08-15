import {
  Box, Paper, Table, TableHead, TableBody, TableRow, TableCell,
  TableSortLabel, TablePagination, TextField, Select, MenuItem,
  Button, ButtonGroup, Tooltip, IconButton, Stack,
  InputAdornment, FormControl, InputLabel, Typography, Divider, Chip,
} from '@mui/material';
import { visuallyHidden } from '@mui/utils';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import TableRowsIcon from '@mui/icons-material/TableRows';
import ClearIcon from '@mui/icons-material/Clear';
import { useState, useMemo } from 'react';
import type { ColumnDef, FilterState, SortState } from './types';
import { exportCsv, exportPdf } from './exportUtils';
import ColumnManager from './ColumnManager';

interface Props<T extends object> {
  rows: T[];
  columns: ColumnDef<T>[];
  title?: string;
  exportFilename?: string;
  pageSize?: number;
  tableId?: string;
}

const OPERATORS = [
  { value: 'contains', label: 'Contains' },
  { value: 'equals', label: 'Equals' },
  { value: 'startsWith', label: 'Starts with' },
];

function applyFilter<T extends object>(row: T, f: FilterState): boolean {
  const val = String((row as Record<string, unknown>)[f.field] ?? '').toLowerCase();
  const term = f.value.toLowerCase();
  if (!term) return true;
  if (f.operator === 'equals') return val === term;
  if (f.operator === 'startsWith') return val.startsWith(term);
  return val.includes(term);
}

export default function AdvancedDataTable<T extends object>({
  rows, columns, title = 'Data', exportFilename = 'export', pageSize: defaultSize = 10, tableId,
}: Props<T>) {
  const storageKey = `col-prefs-${tableId ?? 'default'}`;

  const [visible, setVisible] = useState<Set<string>>(() => {
    try {
      const s = localStorage.getItem(storageKey);
      return s ? new Set<string>(JSON.parse(s) as string[]) : new Set(columns.map((c) => String(c.field)));
    } catch { return new Set(columns.map((c) => String(c.field))); }
  });

  const toggleCol = (field: string) => {
    setVisible((prev) => {
      const next = new Set(prev);
      if (next.has(field)) next.delete(field); else next.add(field);
      localStorage.setItem(storageKey, JSON.stringify([...next]));
      return next;
    });
  };

  const visibleCols = columns.filter((c) => visible.has(String(c.field)) || c.field === 'actions');

  const [search, setSearch]       = useState('');
  const [sort, setSort]           = useState<SortState | null>(null);
  const [page, setPage]           = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(defaultSize);
  const [filters, setFilters]     = useState<FilterState[]>([]);
  const [showFilters, setShowFilters] = useState(false);

  const searched = useMemo(() => !search ? rows : rows.filter((row) =>
    columns.some((col) => String((row as Record<string, unknown>)[col.field as string] ?? '').toLowerCase().includes(search.toLowerCase()))
  ), [rows, search, columns]);

  const filtered = useMemo(() => searched.filter((row) => filters.every((f) => applyFilter(row, f))), [searched, filters]);

  const sorted = useMemo(() => !sort ? filtered : [...filtered].sort((a, b) => {
    const av = String((a as Record<string, unknown>)[sort.field] ?? '');
    const bv = String((b as Record<string, unknown>)[sort.field] ?? '');
    return sort.direction === 'asc' ? av.localeCompare(bv) : bv.localeCompare(av);
  }), [filtered, sort]);

  const paginated = sorted.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const handleSort = (field: string) => {
    setSort((p) => p?.field === field ? { field, direction: p.direction === 'asc' ? 'desc' : 'asc' } : { field, direction: 'asc' });
    setPage(0);
  };

  const addFilter = () => {
    const fc = columns.filter((c) => c.filterable !== false && c.field !== 'actions');
    if (!fc.length) return;
    setFilters((p) => [...p, { field: String(fc[0].field), operator: 'contains', value: '' }]);
  };

  const removeFilter = (i: number) => setFilters((p) => p.filter((_, idx) => idx !== i));
  const updateFilter = (i: number, patch: Partial<FilterState>) => setFilters((p) => p.map((f, idx) => idx === i ? { ...f, ...patch } : f));
  const activeFilterCount = filters.filter((f) => f.value).length;

  return (
    <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
      <Box sx={{ px: 2, py: 1.5, display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
        <Typography variant="subtitle1" component="h2" fontWeight={700} sx={{ flexGrow: 1 }}>
          {title} <Typography component="span" variant="caption" color="text.secondary" ml={1}>{filtered.length} rows</Typography>
        </Typography>
        <TextField size="small" placeholder="Search…" value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }} sx={{ width: 200 }}
          InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment>, endAdornment: search ? <InputAdornment position="end"><IconButton size="small" onClick={() => setSearch('')}><ClearIcon fontSize="small" /></IconButton></InputAdornment> : null }} />
        <Tooltip title="Column filters">
          <IconButton onClick={() => setShowFilters((p) => !p)} color={showFilters ? 'primary' : 'default'}>
            <FilterListIcon />
            {activeFilterCount > 0 && <Chip label={activeFilterCount} size="small" color="primary" sx={{ position: 'absolute', top: 4, right: 4, height: 16, fontSize: '0.6rem' }} />}
          </IconButton>
        </Tooltip>
        <ColumnManager columns={columns} visible={visible} onToggle={toggleCol} />
        <ButtonGroup size="small" variant="outlined">
          <Tooltip title="Export CSV"><Button onClick={() => exportCsv(sorted, columns, exportFilename)} startIcon={<TableRowsIcon fontSize="small" />}>CSV</Button></Tooltip>
          <Tooltip title="Export PDF"><Button onClick={() => exportPdf(sorted, columns, title, exportFilename)} startIcon={<PictureAsPdfIcon fontSize="small" />}>PDF</Button></Tooltip>
        </ButtonGroup>
      </Box>

      {showFilters && (
        <Box sx={{ px: 2, pb: 1.5, bgcolor: 'action.hover' }}>
          <Stack direction="row" flexWrap="wrap" gap={1} alignItems="center">
            {filters.map((f, i) => {
              const fc = columns.filter((c) => c.filterable !== false && c.field !== 'actions');
              return (
                <Stack key={i} direction="row" spacing={1} alignItems="center">
                  <FormControl size="small" sx={{ minWidth: 120 }}>
                    <InputLabel>Field</InputLabel>
                    <Select label="Field" value={f.field} onChange={(e) => updateFilter(i, { field: e.target.value })}>
                      {fc.map((c) => <MenuItem key={String(c.field)} value={String(c.field)}>{c.headerName}</MenuItem>)}
                    </Select>
                  </FormControl>
                  <FormControl size="small" sx={{ minWidth: 120 }}>
                    <InputLabel>Operator</InputLabel>
                    <Select label="Operator" value={f.operator} onChange={(e) => updateFilter(i, { operator: e.target.value as FilterState['operator'] })}>
                      {OPERATORS.map((op) => <MenuItem key={op.value} value={op.value}>{op.label}</MenuItem>)}
                    </Select>
                  </FormControl>
                  <TextField size="small" placeholder="Value" value={f.value} onChange={(e) => { updateFilter(i, { value: e.target.value }); setPage(0); }} sx={{ width: 140 }} />
                  <IconButton size="small" onClick={() => removeFilter(i)} color="error"><ClearIcon fontSize="small" /></IconButton>
                </Stack>
              );
            })}
            <Button size="small" onClick={addFilter} startIcon={<FilterListIcon />}>Add filter</Button>
            {filters.length > 0 && <Button size="small" color="error" onClick={() => setFilters([])}>Clear all</Button>}
          </Stack>
        </Box>
      )}
      <Divider />
      <Box sx={{ overflowX: 'auto' }}>
        <Table size="small">
          <TableHead>
            <TableRow>
              {visibleCols.map((col) => (
                <TableCell key={String(col.field)} width={col.width} sx={{ fontWeight: 700, whiteSpace: 'nowrap' }}>
                  {col.sortable !== false && col.field !== 'actions'
                    ? <TableSortLabel active={sort?.field === String(col.field)} direction={sort?.field === String(col.field) ? sort.direction : 'asc'} onClick={() => handleSort(String(col.field))}>{col.headerName}</TableSortLabel>
                    : col.headerName || (col.field === 'actions' ? <Box component="span" sx={visuallyHidden}>Actions</Box> : null)}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {paginated.length === 0
              ? <TableRow><TableCell colSpan={visibleCols.length} align="center" sx={{ py: 6, color: 'text.secondary' }}>No results found</TableCell></TableRow>
              : paginated.map((row, i) => (
                  <TableRow key={i} hover>
                    {visibleCols.map((col) => (
                      <TableCell key={String(col.field)}>
                        {col.renderCell ? col.renderCell(row) : String((row as Record<string, unknown>)[col.field as string] ?? '')}
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
          </TableBody>
        </Table>
      </Box>
      <TablePagination component="div" count={sorted.length} page={page} onPageChange={(_, p) => setPage(p)}
        rowsPerPage={rowsPerPage} onRowsPerPageChange={(e) => { setRowsPerPage(Number(e.target.value)); setPage(0); }}
        rowsPerPageOptions={[5, 10, 25, 50]} />
    </Paper>
  );
}
