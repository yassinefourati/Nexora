import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useOrganizations, useDeleteOrganization } from '../hooks/useOrganizations';
import OrganizationFormDialog from '../components/OrganizationFormDialog';
import type { Organization } from '../api/organizationsApi';
import { Chip } from '@mui/material';

export default function Organizations() {
  const { t } = useTranslation();
  const columns: ColumnDef<Organization>[] = useMemo(() => [
    { field: 'name', headerName: t('fields.name'), width: 200 },
    { field: 'code', headerName: t('fields.code'), width: 140 },
    { field: 'status', headerName: t('fields.status'), width: 120, renderCell: (row) => <Chip label={row.status} size="small" color={row.status === 'active' ? 'success' : 'default'} /> },
  ], [t]);

  return (
    <CrudPage<Organization>
      resource="organizations"
      useList={useOrganizations}
      useRemove={useDeleteOrganization}
      columns={columns}
      rowLabel={(row) => row.name ?? ''}
      renderDialog={({ open, onClose, editItem }) => <OrganizationFormDialog open={open} onClose={onClose} editOrg={editItem} />}
    ></CrudPage>
  );
}
