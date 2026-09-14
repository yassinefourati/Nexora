import { useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useAuditLog } from '../hooks/useAudit';
import type { AuditLog } from '../api/auditApi';
import { Chip } from '@mui/material';

export default function AuditLog() {
  const { t } = useTranslation();
  const fmt = useCallback((v?: string) => (v ? new Date(v).toLocaleString() : t('common.dash')), [t]);
  const columns: ColumnDef<AuditLog>[] = useMemo(() => [
    { field: 'action', headerName: t('fields.action'), width: 160 },
    { field: 'entityType', headerName: t('fields.entity'), width: 140, renderCell: (row) => <Chip label={row.entityType} size="small" variant="outlined" /> },
    { field: 'entityId', headerName: t('fields.entityId'), width: 220 },
    { field: 'ipAddress', headerName: t('fields.ip'), width: 130 },
    { field: 'createdAt', headerName: t('fields.time'), width: 180, renderCell: (row) => fmt(row.createdAt) },
  ], [t, fmt]);

  return (
    <CrudPage<AuditLog>
      resource="auditLogs"
      permission="audit"
      useList={useAuditLog}
      columns={columns}
      rowLabel={(row) => row.id ?? ''}
      defaultSort={{ field: 'createdAt', direction: 'desc' }}
    ></CrudPage>
  );
}
