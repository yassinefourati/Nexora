import {
  Box, Paper, Table, TableHead, TableBody, TableRow, TableCell,
  TableSortLabel, TablePagination, TextField, Button, Tooltip, IconButton,
  InputAdornment, Typography, Divider, CircularProgress, Alert,
} from '@mui/material';
import { visuallyHidden } from '@mui/utils';
import SearchIcon from '@mui/icons-material/Search';
import ClearIcon from '@mui/icons-material/Clear';
import RefreshIcon from '@mui/icons-material/Refresh';
import { useId, useState } from 'react';
import { useTranslation } from 'react-i18next';
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
 * Server-driven table: search, sort and pagination are controlled props the
 * caller feeds straight into a backend list query. This component never
 * slices or filters `rows`; `rows` is exactly one backend page.
 *
 * Accessibility: sortable headers expose `aria-sort`, every icon button has a
 * label, the search field is labelled, loading is announced, and row clicks
 * are mirrored by a keyboard-reachable control when `onRowClick` is set.
 */
export default function ServerDataTable<T extends object>({
  rows, columns, pagination, isLoading, isError, onRefetch, title,
  page, pageSize, onPageChange, onPageSizeChange,
  sort, onSortChange, search, onSearchChange, rowKey, onRowClick,
}: Props<T>) {
  const { t } = useTranslation();
  const [searchDraft, setSearchDraft] = useState(search);
  const headingId = useId();
  const tableTitle = title ?? t('table.defaultTitle');

  const handleSort = (field: string) => {
    if (sort?.field === field) {
      onSortChange(sort.direction === 'asc' ? { field, direction: 'desc' } : null);
    } else {
      onSortChange({ field, direction: 'asc' });
    }
  };

  const commitSearch = () => { onSearchChange(searchDraft); onPageChange(0); };
  const clearSearch = () => { setSearchDraft(''); onSearchChange(''); onPageChange(0); };

  const ariaSort = (field: string): 'ascending' | 'descending' | 'none' =>
    sort?.field === field ? (sort.direction === 'asc' ? 'ascending' : 'descending') : 'none';

  return (
    <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }} component="section" aria-labelledby={headingId}>
      <Box sx={{ px: 2, py: 1.5, display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
        <Typography id={headingId} variant="subtitle1" component="h2" fontWeight={700} sx={{ flexGrow: 1 }}>
          {tableTitle}
          {pagination && (
            <Typography component="span" variant="caption" color="text.secondary" ml={1}>
              {t('table.rowCount', { count: pagination.totalElements })}
            </Typography>
          )}
        </Typography>
        <TextField
          size="small"
          label={t('table.search')}
          placeholder={t('table.searchPlaceholder')}
          value={searchDraft}
          onChange={(e) => setSearchDraft(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') commitSearch(); if (e.key === 'Escape') clearSearch(); }}
          sx={{ width: 240 }}
          slotProps={{ input: {
            startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment>,
            endAdornment: searchDraft ? (
              <InputAdornment position="end">
                <IconButton size="small" aria-label={t('table.clearSearch')} onClick={clearSearch}>
                  <ClearIcon fontSize="small" />
                </IconButton>
              </InputAdornment>
            ) : null,
          } }}
        />
        <Button size="small" variant="outlined" onClick={commitSearch}>{t('table.search')}</Button>
        {onRefetch && (
          <Tooltip title={t('table.refresh')}>
            <IconButton size="small" aria-label={t('table.refresh')} onClick={onRefetch}><RefreshIcon fontSize="small" /></IconButton>
          </Tooltip>
        )}
      </Box>
      <Divider />

      {isError ? (
        <Alert severity="error" sx={{ m: 2 }} action={onRefetch ? <Button color="inherit" size="small" onClick={onRefetch}>{t('common.tryAgain')}</Button> : undefined}>
          {t('table.loadFailed')}
        </Alert>
      ) : (
        <Box sx={{ overflowX: 'auto', position: 'relative' }} aria-busy={isLoading || undefined}>
          {isLoading && (
            <Box role="status" sx={{ position: 'absolute', inset: 0, bgcolor: 'background.paper', opacity: 0.6, display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1 }}>
              <CircularProgress size={28} aria-label={t('common.loading')} />
            </Box>
          )}
          <Table size="small" aria-label={tableTitle}>
            <TableHead>
              <TableRow>
                {columns.map((col) => {
                  const field = String(col.field);
                  const sortable = col.sortable !== false && col.field !== 'actions';
                  return (
                    <TableCell
                      key={field}
                      width={col.width}
                      sx={{ fontWeight: 700, whiteSpace: 'nowrap' }}
                      aria-sort={sortable ? ariaSort(field) : undefined}
                    >
                      {sortable
                        ? (
                          <TableSortLabel
                            active={sort?.field === field}
                            direction={sort?.field === field ? sort.direction : 'asc'}
                            onClick={() => handleSort(field)}
                          >
                            {col.headerName}
                            {sort?.field === field && (
                              <Box component="span" sx={visuallyHidden}>
                                {sort.direction === 'desc' ? t('table.sortedDescending') : t('table.sortedAscending')}
                              </Box>
                            )}
                          </TableSortLabel>
                        )
                        : col.headerName || (col.field === 'actions' ? <Box component="span" sx={visuallyHidden}>{t('table.actions')}</Box> : null)}
                    </TableCell>
                  );
                })}
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.length === 0 && !isLoading
                ? <TableRow><TableCell colSpan={columns.length} align="center" sx={{ py: 6, color: 'text.secondary' }}>{t('table.noResults')}</TableCell></TableRow>
                : rows.map((row) => (
                    <TableRow
                      key={rowKey(row)}
                      hover
                      onClick={onRowClick ? () => onRowClick(row) : undefined}
                      onKeyDown={onRowClick ? (e) => { if (e.key === 'Enter') onRowClick(row); } : undefined}
                      tabIndex={onRowClick ? 0 : undefined}
                      sx={onRowClick ? { cursor: 'pointer' } : undefined}
                    >
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
        labelRowsPerPage={t('table.rowsPerPage')}
        labelDisplayedRows={({ from, to, count }) => t('table.displayedRows', { from, to, count })}
        getItemAriaLabel={(type) => t(`table.page.${type}`)}
      />
    </Paper>
  );
}
