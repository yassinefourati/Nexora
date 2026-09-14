import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useSessionsList } from '../hooks/useSessions';
import type { Session } from '../api/sessionsApi';
import { Chip } from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import { useRevokeSession } from '../hooks/useSessions';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';

export default function Sessions() {
  const { t } = useTranslation();
  const { mutate: revoke } = useRevokeSession();
  const { confirm } = useConfirmStore();
  const columns: ColumnDef<Session>[] = useMemo(() => [
    { field: 'ipAddress', headerName: t('fields.ip'), width: 140 },
    { field: 'userAgent', headerName: t('fields.userAgent'), width: 240 },
    { field: 'revokedAt', headerName: t('fields.status'), width: 110, renderCell: (row) => <Chip label={row.revokedAt ? t('fields.revoked') : t('fields.active')} size="small" color={row.revokedAt ? 'default' : 'success'} /> },
    { field: 'expiresAt', headerName: t('fields.expires'), width: 180, renderCell: (row) => row.expiresAt ? new Date(row.expiresAt).toLocaleString() : t('common.dash') },
  ], [t]);

  return (
    <CrudPage<Session>
      resource="sessions"
      useList={useSessionsList}
      columns={columns}
      rowLabel={(row) => row.ipAddress ?? ''}
      defaultSort={{ field: 'createdAt', direction: 'desc' }}
      rowActions={(row) => row.revokedAt ? [] : [{
        label: t('common.revoke'), icon: <BlockIcon fontSize="small" color="error" />, color: 'error',
        onClick: () => confirm({ title: t('resources.sessions.revokeTitle'), message: t('resources.sessions.revokeMessage'), confirmLabel: t('common.revoke'), severity: 'error', onConfirm: () => revoke(row.id!) }),
      }]}
    ></CrudPage>
  );
}
