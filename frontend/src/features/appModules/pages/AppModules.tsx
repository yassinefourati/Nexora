import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useAppModules, useDeleteAppModule } from '../hooks/useAppModules';
import AppModuleFormDialog from '../components/AppModuleFormDialog';
import type { AppModule } from '../api/appModulesApi';
import { Chip } from '@mui/material';

export default function AppModules() {
  const { t } = useTranslation();
  const columns: ColumnDef<AppModule>[] = useMemo(() => [
    { field: 'key', headerName: t('fields.key'), width: 160 },
    { field: 'name', headerName: t('fields.name'), width: 200 },
    { field: 'description', headerName: t('fields.description'), width: 260 },
    { field: 'active', headerName: t('fields.active'), width: 100, renderCell: (row) => <Chip label={row.active ? t('fields.active') : t('fields.inactive')} size="small" color={row.active ? 'success' : 'default'} /> },
  ], [t]);

  return (
    <CrudPage<AppModule>
      resource="appModules"
      useList={useAppModules}
      useRemove={useDeleteAppModule}
      columns={columns}
      rowLabel={(row) => row.name ?? ''}
      renderDialog={({ open, onClose, editItem }) => <AppModuleFormDialog open={open} onClose={onClose} editModule={editItem} />}
    ></CrudPage>
  );
}
