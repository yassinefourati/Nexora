import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useTeams, useDeleteTeam } from '../hooks/useTeams';
import TeamFormDialog from '../components/TeamFormDialog';
import type { Team } from '../api/teamsApi';
import { useDepartments } from '@/features/departments/hooks/useDepartments';

export default function Teams() {
  const { t } = useTranslation();
  const { data: depts } = useDepartments({ page: 0, size: 200 });
  const deptName = useMemo(() => { const m = new Map((depts?.items ?? []).map((d) => [d.id, d.name])); return (id?: string) => (id && m.get(id)) || t('common.dash'); }, [depts, t]);
  const columns: ColumnDef<Team>[] = useMemo(() => [
    { field: 'name', headerName: t('fields.name'), width: 220 },
    { field: 'departmentId', headerName: t('fields.department'), width: 220, renderCell: (row) => deptName(row.departmentId) },
  ], [t, deptName]);

  return (
    <CrudPage<Team>
      resource="teams"
      useList={useTeams}
      useRemove={useDeleteTeam}
      columns={columns}
      rowLabel={(row) => row.name ?? ''}
      renderDialog={({ open, onClose, editItem }) => <TeamFormDialog open={open} onClose={onClose} editTeam={editItem} />}
    ></CrudPage>
  );
}
