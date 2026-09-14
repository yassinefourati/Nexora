import { useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useAuthLogs } from '../hooks/useAuthLogs';
import type { AuthLog } from '../api/authLogsApi';
import { Chip } from '@mui/material';

export default function AuthLogs() {
  const { t } = useTranslation();
  const fmt = useCallback((v?: string) => (v ? new Date(v).toLocaleString() : t('common.dash')), [t]);
  const columns: ColumnDef<AuthLog>[] = useMemo(() => [
    { field: 'eventType', headerName: t('fields.event'), width: 160, renderCell: (row) => <Chip label={row.eventType} size="small" variant="outlined" /> },
    { field: 'ipAddress', headerName: t('fields.ip'), width: 140 },
    { field: 'userAgent', headerName: t('fields.userAgent'), width: 260 },
    { field: 'createdAt', headerName: t('fields.time'), width: 180, renderCell: (row) => fmt(row.createdAt) },
  ], [t, fmt]);

  return (
    <CrudPage<AuthLog>
      resource="authLogs"
      useList={useAuthLogs}
      columns={columns}
      rowLabel={(row) => row.id ?? ''}
      defaultSort={{ field: 'createdAt', direction: 'desc' }}
    ></CrudPage>
  );
}
