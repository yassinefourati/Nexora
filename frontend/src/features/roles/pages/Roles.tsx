import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useRoles, useDeleteRole } from '../hooks/useRoles';
import RoleFormDialog from '../components/RoleFormDialog';
import type { Role } from '../api/rolesApi';
import { Chip } from '@mui/material';

export default function Roles() {
  const { t } = useTranslation();
  const columns: ColumnDef<Role>[] = useMemo(() => [
    { field: 'name', headerName: t('fields.name'), width: 180 },
    { field: 'description', headerName: t('fields.description'), width: 300 },
    { field: 'system', headerName: t('fields.system'), width: 110, renderCell: (row) => row.system ? <Chip label={t('fields.system')} size="small" color="warning" /> : null },
  ], [t]);

  return (
    <CrudPage<Role>
      resource="roles"
      permission="role"
      useList={useRoles}
      useRemove={useDeleteRole}
      columns={columns}
      rowLabel={(row) => row.name ?? ''}
      renderDialog={({ open, onClose, editItem }) => <RoleFormDialog open={open} onClose={onClose} editRole={editItem} />}
    ></CrudPage>
  );
}
