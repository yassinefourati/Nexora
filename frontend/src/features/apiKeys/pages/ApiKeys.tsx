import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useApiKeysList } from '../hooks/useApiKeys';
import ApiKeyFormDialog from '../components/ApiKeyFormDialog';
import type { ApiKey } from '../api/apiKeysApi';
import { Chip } from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import { useRevokeApiKey } from '../hooks/useApiKeys';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';

export default function ApiKeys() {
  const { t } = useTranslation();
  const { mutate: revoke } = useRevokeApiKey();
  const { confirm } = useConfirmStore();
  const columns: ColumnDef<ApiKey>[] = useMemo(() => [
    { field: 'name', headerName: t('fields.name'), width: 180 },
    { field: 'scopes', headerName: t('fields.scopes'), width: 220, renderCell: (row) => (row.scopes ?? []).map((s) => <Chip key={s} label={s} size="small" sx={{ mr: 0.5 }} />) },
    { field: 'revokedAt', headerName: t('fields.status'), width: 110, renderCell: (row) => <Chip label={row.revokedAt ? t('fields.revoked') : t('fields.active')} size="small" color={row.revokedAt ? 'default' : 'success'} /> },
    { field: 'lastUsedAt', headerName: t('fields.lastUsed'), width: 180, renderCell: (row) => row.lastUsedAt ? new Date(row.lastUsedAt).toLocaleString() : t('fields.never') },
    { field: 'expiresAt', headerName: t('fields.expires'), width: 180, renderCell: (row) => row.expiresAt ? new Date(row.expiresAt).toLocaleString() : t('fields.never') },
  ], [t]);

  return (
    <CrudPage<ApiKey>
      resource="apiKeys"
      useList={useApiKeysList}
      columns={columns}
      rowLabel={(row) => row.name ?? ''}
      defaultSort={{ field: 'createdAt', direction: 'desc' }}
      renderDialog={({ open, onClose }) => <ApiKeyFormDialog open={open} onClose={onClose} />}
      rowActions={(row) => row.revokedAt ? [] : [{
        label: t('common.revoke'), icon: <BlockIcon fontSize="small" color="error" />, color: 'error',
        onClick: () => confirm({ title: t('resources.apiKeys.revokeTitle'), message: t('resources.apiKeys.revokeMessage', { name: row.name }), confirmLabel: t('common.revoke'), severity: 'error', onConfirm: () => revoke(row.id!) }),
      }]}
    ></CrudPage>
  );
}
