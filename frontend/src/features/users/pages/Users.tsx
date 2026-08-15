import { useState, useCallback, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useUsers, useDeleteUser } from '../hooks/useUsers';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import Can from '@/shared/components/Can';
import { usePermission } from '@/shared/hooks/usePermission';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import PageHeader from '@/shared/components/PageHeader';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import UserFormDialog from '../components/UserFormDialog';
import type { User } from '../api/usersApi';

export default function Users() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editUser, setEditUser] = useState<User | null>(null);

  const { data, isLoading, isError, refetch } = useUsers({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });

  const { mutate: remove } = useDeleteUser();
  const { confirm } = useConfirmStore();
  const canEdit = usePermission('user', 'edit');
  const canDelete = usePermission('user', 'delete');

  // The real backend has no text-search query param on GET /users — filter client-side
  // over the current page only, as a convenience, not a substitute for server-side search.
  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((u) =>
      [u.username, u.email, u.firstName, u.lastName].some((v) => v?.toLowerCase().includes(term))
    );
  }, [data, search]);

  const columns: ColumnDef<User>[] = useMemo(() => [
    { field: 'username', headerName: t('users.name'), width: 160 },
    { field: 'email', headerName: t('users.email'), width: 220 },
    {
      field: 'status', headerName: 'Status', width: 130,
      renderCell: (row) => (
        <Chip
          icon={row.status === 'active' ? <CheckCircleIcon /> : <CancelIcon />}
          label={row.status}
          size="small"
          color={row.status === 'active' ? 'success' : 'default'}
          variant={row.status === 'active' ? 'filled' : 'outlined'}
        />
      ),
    },
    {
      field: 'superuser', headerName: 'Superuser', width: 110,
      renderCell: (row) => row.superuser ? <Chip label="Yes" size="small" color="error" variant="outlined" /> : '—',
    },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            {
              label: t('common.edit'),
              icon: <EditIcon fontSize="small" />,
              hidden: !canEdit,
              onClick: () => { setEditUser(row); setDialogOpen(true); },
            },
            {
              label: t('common.delete'),
              icon: <DeleteIcon fontSize="small" color="error" />,
              color: 'error',
              hidden: !canDelete,
              onClick: () => confirm({
                title: t('users.deleteConfirmTitle'),
                message: t('users.deleteConfirmMessage', { name: row.username }),
                confirmLabel: t('common.delete'),
                severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [t, confirm, remove, canEdit, canDelete]);

  const handleRowClick = useCallback((row: User) => navigate(`/users/${row.id}`), [navigate]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title={t('users.title')}
        description="Manage team members and account access."
        actions={
          <Can resource="user" action="write">
            <Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditUser(null); setDialogOpen(true); }}>
              {t('users.addUser')}
            </Button>
          </Can>
        }
      />

      <ServerDataTable
        title={t('users.title')}
        rows={rows}
        columns={columns}
        pagination={data?.pagination}
        isLoading={isLoading}
        isError={isError}
        onRefetch={() => void refetch()}
        page={page}
        pageSize={pageSize}
        onPageChange={setPage}
        onPageSizeChange={setPageSize}
        sort={sort}
        onSortChange={(s) => { setSort(s); setPage(0); }}
        search={search}
        onSearchChange={setSearch}
        rowKey={(row) => row.id!}
        onRowClick={handleRowClick}
      />

      <UserFormDialog
        open={dialogOpen}
        onClose={() => { setDialogOpen(false); setEditUser(null); }}
        editUser={editUser}
      />
    </Box>
  );
}
