import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useDepartments, useDeleteDepartment } from '../hooks/useDepartments';
import DepartmentFormDialog from '../components/DepartmentFormDialog';
import type { Department } from '../api/departmentsApi';
import { useOrganizations } from '@/features/organizations/hooks/useOrganizations';

export default function Departments() {
  const { t } = useTranslation();
  const { data: orgs } = useOrganizations({ page: 0, size: 200 });
  const orgName = useMemo(() => { const m = new Map((orgs?.items ?? []).map((o) => [o.id, o.name])); return (id?: string) => (id && m.get(id)) || t('common.dash'); }, [orgs, t]);
  const columns: ColumnDef<Department>[] = useMemo(() => [
    { field: 'name', headerName: t('fields.name'), width: 200 },
    { field: 'code', headerName: t('fields.code'), width: 140 },
    { field: 'organizationId', headerName: t('fields.organization'), width: 220, renderCell: (row) => orgName(row.organizationId) },
  ], [t, orgName]);

  return (
    <CrudPage<Department>
      resource="departments"
      useList={useDepartments}
      useRemove={useDeleteDepartment}
      columns={columns}
      rowLabel={(row) => row.name ?? ''}
      renderDialog={({ open, onClose, editItem }) => <DepartmentFormDialog open={open} onClose={onClose} editDept={editItem} />}
    ></CrudPage>
  );
}
