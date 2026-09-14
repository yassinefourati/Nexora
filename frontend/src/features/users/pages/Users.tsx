import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useUsers, useDeleteUser } from '../hooks/useUsers';
import UserFormDialog from '../components/UserFormDialog';
import type { User } from '../api/usersApi';
import { Chip } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import { useNavigate } from 'react-router-dom';

export default function Users() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const columns: ColumnDef<User>[] = useMemo(() => [
    { field: 'username', headerName: t('fields.username'), width: 160 },
    { field: 'email', headerName: t('fields.email'), width: 220 },
    { field: 'status', headerName: t('fields.status'), width: 130, renderCell: (row) => <Chip icon={row.status === 'active' ? <CheckCircleIcon /> : <CancelIcon />} label={row.status} size="small" color={row.status === 'active' ? 'success' : 'default'} variant="outlined" /> },
    { field: 'firstName', headerName: t('fields.firstName'), width: 140 },
    { field: 'lastName', headerName: t('fields.lastName'), width: 140 },
  ], [t]);

  return (
    <CrudPage<User>
      resource="users"
      permission="user"
      useList={useUsers}
      useRemove={useDeleteUser}
      columns={columns}
      rowLabel={(row) => row.username ?? ''}
      renderDialog={({ open, onClose, editItem }) => <UserFormDialog open={open} onClose={onClose} editUser={editItem} />}
      onRowClick={(row) => navigate(`/users/${row.id}`)}
    ></CrudPage>
  );
}
