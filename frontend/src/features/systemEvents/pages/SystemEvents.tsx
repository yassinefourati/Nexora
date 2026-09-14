import { useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useSystemEvents } from '../hooks/useSystemEvents';
import type { SystemEvent } from '../api/systemEventsApi';
import { Chip } from '@mui/material';

const SEVERITY_COLOR: Record<string, 'error' | 'warning' | 'info' | 'default'> = { critical: 'error', error: 'error', warning: 'warning', info: 'info' };

export default function SystemEvents() {
  const { t } = useTranslation();
  const fmt = useCallback((v?: string) => (v ? new Date(v).toLocaleString() : t('common.dash')), [t]);
  const columns: ColumnDef<SystemEvent>[] = useMemo(() => [
    { field: 'eventType', headerName: t('fields.event'), width: 180 },
    { field: 'severity', headerName: t('fields.severity'), width: 110, renderCell: (row) => <Chip label={row.severity} size="small" color={SEVERITY_COLOR[row.severity ?? ''] ?? 'default'} /> },
    { field: 'source', headerName: t('fields.source'), width: 160 },
    { field: 'createdAt', headerName: t('fields.time'), width: 180, renderCell: (row) => fmt(row.createdAt) },
  ], [t, fmt]);

  return (
    <CrudPage<SystemEvent>
      resource="systemEvents"
      useList={useSystemEvents}
      columns={columns}
      rowLabel={(row) => row.id ?? ''}
      defaultSort={{ field: 'createdAt', direction: 'desc' }}
    ></CrudPage>
  );
}
