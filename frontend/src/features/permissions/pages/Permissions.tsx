import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { usePermissionsList, useDeletePermission } from '../hooks/usePermissions';
import PermissionFormDialog from '../components/PermissionFormDialog';
import type { Permission } from '../api/permissionsApi';
import { Chip } from '@mui/material';

export default function Permissions() {
  const { t } = useTranslation();
  const columns: ColumnDef<Permission>[] = useMemo(() => [
    { field: 'code', headerName: t('fields.code'), width: 220 },
    { field: 'resource', headerName: t('fields.resource'), width: 160 },
    { field: 'action', headerName: t('fields.action'), width: 120, renderCell: (row) => <Chip label={row.action} size="small" variant="outlined" /> },
    { field: 'description', headerName: t('fields.description'), width: 260 },
  ], [t]);

  return (
    <CrudPage<Permission>
      resource="permissions"
      permission="role"
      useList={usePermissionsList}
      useRemove={useDeletePermission}
      columns={columns}
      rowLabel={(row) => row.code ?? ''}
      editable={false}
      renderDialog={({ open, onClose }) => <PermissionFormDialog open={open} onClose={onClose} />}
    ></CrudPage>
  );
}
