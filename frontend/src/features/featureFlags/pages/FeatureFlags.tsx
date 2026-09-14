import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useFeatureFlags, useDeleteFeatureFlag } from '../hooks/useFeatureFlags';
import FeatureFlagFormDialog from '../components/FeatureFlagFormDialog';
import type { FeatureFlag } from '../api/featureFlagsApi';
import { useCallback } from 'react';
import { Switch } from '@mui/material';
import { useUpdateFeatureFlag } from '../hooks/useFeatureFlags';

export default function FeatureFlags() {
  const { t } = useTranslation();
  const { mutate: update } = useUpdateFeatureFlag();
  const toggle = useCallback((flag: FeatureFlag) => {
    update({ id: flag.id!, body: { name: flag.name!, description: flag.description, enabled: !flag.enabled, version: flag.version ?? undefined } });
  }, [update]);
  const columns: ColumnDef<FeatureFlag>[] = useMemo(() => [
    { field: 'key', headerName: t('fields.key'), width: 180 },
    { field: 'name', headerName: t('fields.name'), width: 200 },
    { field: 'description', headerName: t('fields.description'), width: 240 },
    { field: 'enabled', headerName: t('fields.enabled'), width: 100, renderCell: (row) => <Switch checked={row.enabled ?? false} size="small" inputProps={{ 'aria-label': t('fields.enabled') }} onChange={() => toggle(row)} /> },
  ], [t, toggle]);

  return (
    <CrudPage<FeatureFlag>
      resource="featureFlags"
      useList={useFeatureFlags}
      useRemove={useDeleteFeatureFlag}
      columns={columns}
      rowLabel={(row) => row.name ?? ''}
      renderDialog={({ open, onClose, editItem }) => <FeatureFlagFormDialog open={open} onClose={onClose} editFlag={editItem} />}
    ></CrudPage>
  );
}
