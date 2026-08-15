import { useState, useMemo } from 'react';
import { Box, Button, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import { useTranslation } from 'react-i18next';
import { useSettings, useDeleteSetting } from '../hooks/useSettings';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import SettingFormDialog from '../components/SettingFormDialog';
import type { Setting } from '../api/settingsApi';

export default function Settings() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editSetting, setEditSetting] = useState<Setting | null>(null);

  const { data, isLoading, isError, refetch } = useSettings({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteSetting();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((s) => s.key?.toLowerCase().includes(term) || s.description?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<Setting>[] = useMemo(() => [
    { field: 'key', headerName: 'Key', width: 200 },
    { field: 'scope', headerName: 'Scope', width: 110, renderCell: (row) => <Chip label={row.scope} size="small" variant="outlined" /> },
    { field: 'value', headerName: 'Value', width: 220 },
    { field: 'editable', headerName: 'Editable', width: 100, renderCell: (row) => row.editable ? 'Yes' : 'No' },
    {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: t('common.edit'), icon: <EditIcon fontSize="small" />, hidden: !row.editable, onClick: () => { setEditSetting(row); setDialogOpen(true); } },
            {
              label: t('common.delete'), icon: <DeleteIcon fontSize="small" color="error" />, color: 'error', hidden: !row.editable,
              onClick: () => confirm({
                title: 'Delete setting', message: `Delete setting "${row.key}"?`, confirmLabel: t('common.delete'), severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [t, confirm, remove]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <PageHeader
        title={t('settings.general')}
        description="Global and per-organization configuration key/value pairs."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditSetting(null); setDialogOpen(true); }}>Add Setting</Button>}
      />
      <ServerDataTable
        title={t('settings.general')}
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
      />
      <SettingFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditSetting(null); }} editSetting={editSetting} />
    </Box>
  );
}
