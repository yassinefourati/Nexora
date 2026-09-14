import { useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useLoginHistory } from '../hooks/useLoginHistory';
import type { LoginHistory } from '../api/loginHistoryApi';
import { Chip } from '@mui/material';

export default function LoginHistory() {
  const { t } = useTranslation();
  const fmt = useCallback((v?: string) => (v ? new Date(v).toLocaleString() : t('common.dash')), [t]);
  const columns: ColumnDef<LoginHistory>[] = useMemo(() => [
    { field: 'success', headerName: t('fields.result'), width: 100, renderCell: (row) => <Chip label={row.success ? t('fields.success') : t('fields.failed')} size="small" color={row.success ? 'success' : 'error'} /> },
    { field: 'ipAddress', headerName: t('fields.ip'), width: 140 },
    { field: 'userAgent', headerName: t('fields.userAgent'), width: 240 },
    { field: 'loginAt', headerName: t('fields.loginAt'), width: 180, renderCell: (row) => fmt(row.loginAt) },
    { field: 'logoutAt', headerName: t('fields.logoutAt'), width: 180, renderCell: (row) => fmt(row.logoutAt) },
  ], [t, fmt]);

  return (
    <CrudPage<LoginHistory>
      resource="loginHistory"
      useList={useLoginHistory}
      columns={columns}
      rowLabel={(row) => row.id ?? ''}
      defaultSort={{ field: 'loginAt', direction: 'desc' }}
    ></CrudPage>
  );
}
