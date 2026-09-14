import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useSettings, useDeleteSetting } from '../hooks/useSettings';
import SettingFormDialog from '../components/SettingFormDialog';
import type { Setting } from '../api/settingsApi';
import { Chip } from '@mui/material';

export default function Settings() {
  const { t } = useTranslation();
  const columns: ColumnDef<Setting>[] = useMemo(() => [
    { field: 'key', headerName: t('fields.key'), width: 200 },
    { field: 'scope', headerName: t('fields.scope'), width: 110, renderCell: (row) => <Chip label={row.scope} size="small" variant="outlined" /> },
    { field: 'value', headerName: t('fields.value'), width: 220 },
    { field: 'editable', headerName: t('fields.editable'), width: 100, renderCell: (row) => row.editable ? t('fields.yes') : t('fields.no') },
  ], [t]);

  return (
    <CrudPage<Setting>
      resource="settings"
      permission="settings"
      useList={useSettings}
      useRemove={useDeleteSetting}
      columns={columns}
      rowLabel={(row) => row.key ?? ''}
      renderDialog={({ open, onClose, editItem }) => <SettingFormDialog open={open} onClose={onClose} editSetting={editItem} />}
    ></CrudPage>
  );
}
