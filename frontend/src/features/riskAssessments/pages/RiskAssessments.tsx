import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import AddIcon from '@mui/icons-material/Add';
import { useRiskAssessments, useProcessRiskAssessment } from '../hooks/useRiskAssessments';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import RiskAssessmentFormDialog from '../components/RiskAssessmentFormDialog';
import type { RiskAssessment } from '../api/riskAssessmentsApi';

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  pending: 'default',
  in_progress: 'info',
  completed: 'success',
  failed: 'error',
};

const RISK_CLASS_COLOR: Record<string, 'success' | 'warning' | 'error'> = {
  low: 'success',
  medium: 'warning',
  high: 'error',
  very_high: 'error',
};

export default function RiskAssessments() {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);

  const { data, isLoading, isError, refetch } = useRiskAssessments({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: process } = useProcessRiskAssessment();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((r) => r.status?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<RiskAssessment>[] = useMemo(() => [
    { field: 'status', headerName: 'Status', width: 140, renderCell: (row) => <Chip label={row.status} size="small" color={STATUS_COLOR[row.status ?? 'pending']} /> },
    { field: 'riskScore', headerName: 'Score', width: 100 },
    { field: 'riskClass', headerName: 'Risk class', width: 130, renderCell: (row) => row.riskClass ? <Chip label={row.riskClass.replace('_', ' ')} size="small" color={RISK_CLASS_COLOR[row.riskClass]} /> : '—' },
    { field: 'assessedAt', headerName: 'Assessed', width: 200, renderCell: (row) => row.assessedAt ? new Date(row.assessedAt).toLocaleString() : '—' },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            ...(row.status === 'pending' ? [
              { label: 'Process', icon: <PlayArrowIcon fontSize="small" />, onClick: () => process(row.id!) },
            ] : []),
          ]}
        />
      ),
    },
  ], [process]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title="Risk Assessments"
        description="Manage overall risk assessments performed for loan applications."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>Open Risk Assessment</Button>}
      />
      <ServerDataTable
        title="Risk Assessments"
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
      <RiskAssessmentFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </Box>
  );
}
