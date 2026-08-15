import {
  Box, Paper, Table, TableHead, TableBody, TableRow, TableCell,
  TableSortLabel, TablePagination, TextField, Button, Tooltip, IconButton,
  InputAdornment, Typography, Divider, CircularProgress, Alert,
} from '@mui/material';
import { visuallyHidden } from '@mui/utils';
import SearchIcon from '@mui/icons-material/Search';
import ClearIcon from '@mui/icons-material/Clear';
import RefreshIcon from '@mui/icons-material/Refresh';
import { useState } from 'react';
import type { ColumnDef } from './types';
import type { ApiPagination } from '@/core/api/envelope';

export interface ServerSort { field: string; direction: 'asc' | 'desc'; }

interface Props<T extends object> {
  rows: T[];
  columns: ColumnDef<T>[];
  pagination?: ApiPagination;
  isLoading?: boolean;
  isError?: boolean;
  onRefetch?: () => void;
  title?: string;
  page: number;                 // zero-based, controlled by the caller
  pageSize: number;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
  sort: ServerSort | null;
  onSortChange: (sort: ServerSort | null) => void;
  search: string;
  onSearchChange: (search: string) => void;
  rowKey: (row: T) => string;
  onRowClick?: (row: T) => void;
}

/**
 * Server-driven counterpart to AdvancedDataTable: search/sort/pagination are
 * all controlled props that the caller feeds straight into a backend list
 * query (see useCrudResource.useList) — this component never slices or
 * filters `rows` itself, since `rows` is already exactly one backend page.
 */
export default function ServerDataTable<T extends object>({
  rows, columns, pagination, isLoading, isError, onRefetch, title = 'Data',
  page, pageSize, onPageChange, onPageSizeChange,
  sort, onSortChange, search, onSearchChange, rowKey, onRowClick,
}: Props<T>) {
  const [searchDraft, setSearchDraft] = useState(search);

  const handleSort = (field: string) => {
    if (sort?.field === field) {
      onSortChange(sort.direction === 'asc' ? { field, direction: 'desc' } : null);
    } else {
      onSortChange({ field, direction: 'asc' });
    }
  };

  const commitSearch = () => { onSearchChange(searchDraft); onPageChange(0); };

  return (
    <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
      <Box sx={{ px: 2, py: 1.5, display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
        <Typography variant="subtitle1" component="h2" fontWeight={700} sx={{ flexGrow: 1 }}>
          {title}
          {pagination && (
            <Typography component="span" variant="caption" color="text.secondary" ml={1}>
              {pagination.totalElements} rows
            </Typography>
          )}
        </Typography>
        <TextField
          size="small" placeholder="Search…" value={searchDraft}
          onChange={(e) => setSearchDraft(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') commitSearch(); }}
          sx={{ width: 220 }}
          slotProps={{ input: {
            startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment>,
            endAdornment: searchDraft ? (
              <InputAdornment position="end">
                <IconButton size="small" onClick={() => { setSearchDraft(''); onSearchChange(''); onPageChange(0); }}>
                  <ClearIcon fontSize="small" />
                </IconButton>
              </InputAdornment>
            ) : null,
          } }}
        />
        <Button size="small" variant="outlined" onClick={commitSearch}>Search</Button>
        {onRefetch && (
          <Tooltip title="Refresh">
            <IconButton size="small" onClick={onRefetch}><RefreshIcon fontSize="small" /></IconButton>
          </Tooltip>
        )}
      </Box>
      <Divider />

      {isError ? (
        <Alert severity="error" sx={{ m: 2 }}>Failed to load data from the server.</Alert>
      ) : (
        <Box sx={{ overflowX: 'auto', position: 'relative' }}>
          {isLoading && (
            <Box sx={{ position: 'absolute', inset: 0, bgcolor: 'background.paper', opacity: 0.6, display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1 }}>
              <CircularProgress size={28} aria-label="Loading" />
            </Box>
          )}
          <Table size="small">
            <TableHead>
              <TableRow>
                {columns.map((col) => (
                  <TableCell key={String(col.field)} width={col.width} sx={{ fontWeight: 700, whiteSpace: 'nowrap' }}>
                    {col.sortable !== false && col.field !== 'actions'
                      ? (
                        <TableSortLabel
                          active={sort?.field === String(col.field)}
                          direction={sort?.field === String(col.field) ? sort.direction : 'asc'}
                          onClick={() => handleSort(String(col.field))}
                        >
                          {col.headerName}
                        </TableSortLabel>
                      )
                      : col.headerName || (col.field === 'actions' ? <Box component="span" sx={visuallyHidden}>Actions</Box> : null)}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.length === 0 && !isLoading
                ? <TableRow><TableCell colSpan={columns.length} align="center" sx={{ py: 6, color: 'text.secondary' }}>No results found</TableCell></TableRow>
                : rows.map((row) => (
                    <TableRow key={rowKey(row)} hover onClick={() => onRowClick?.(row)} sx={onRowClick ? { cursor: 'pointer' } : undefined}>
                      {columns.map((col) => (
                        <TableCell key={String(col.field)} onClick={col.field === 'actions' ? (e) => e.stopPropagation() : undefined}>
                          {col.renderCell ? col.renderCell(row) : String((row as Record<string, unknown>)[col.field as string] ?? '')}
                        </TableCell>
                      ))}
                    </TableRow>
                  ))}
            </TableBody>
          </Table>
        </Box>
      )}

      <TablePagination
        component="div"
        count={pagination?.totalElements ?? 0}
        page={page}
        onPageChange={(_, p) => onPageChange(p)}
        rowsPerPage={pageSize}
        onRowsPerPageChange={(e) => { onPageSizeChange(Number(e.target.value)); onPageChange(0); }}
        rowsPerPageOptions={[10, 20, 50, 100]}
      />
    </Paper>
  );
}
