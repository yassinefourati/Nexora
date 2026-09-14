import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Button, Chip, Paper, Typography, Stack } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import ListAltIcon from '@mui/icons-material/ListAlt';
import CrudPage from '@/shared/components/crud/CrudPage';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useMenus, useDeleteMenu, useMenuItems, useDeleteMenuItem } from '../hooks/useMenus';
import MenuFormDialog from '../components/MenuFormDialog';
import MenuItemFormDialog from '../components/MenuItemFormDialog';
import type { Menu, MenuItem } from '../api/menusApi';

function MenuItemsPanel({ menu }: { menu: Menu }) {
  const { t } = useTranslation();
  const { data, isLoading } = useMenuItems({ menuId: menu.id, page: 0, size: 100 });
  const { mutate: remove } = useDeleteMenuItem();
  const { confirm } = useConfirmStore();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editItem, setEditItem] = useState<MenuItem | null>(null);
  const items = data?.items ?? [];
  return (
    <Paper variant="outlined" sx={{ p: 2, borderRadius: 2 }} component="section" aria-label={t('resources.menus.itemsIn', { name: menu.name })}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={1.5}>
        <Typography variant="subtitle2" component="h2" fontWeight={700}>{t('resources.menus.itemsIn', { name: menu.name })}</Typography>
        <Button size="small" startIcon={<AddIcon fontSize="small" />} onClick={() => { setEditItem(null); setDialogOpen(true); }}>{t('resources.menus.addItem')}</Button>
      </Stack>
      {isLoading ? (
        <Typography variant="body2" color="text.secondary">{t('common.loading')}</Typography>
      ) : items.length === 0 ? (
        <Typography variant="body2" color="text.secondary">{t('resources.menus.noItems')}</Typography>
      ) : (
        <Stack spacing={1} component="ul" sx={{ listStyle: 'none', p: 0, m: 0 }}>
          {items.map((item) => (
            <Stack key={item.id} component="li" direction="row" alignItems="center" spacing={1} sx={{ py: 0.5 }}>
              <Chip label={item.sortOrder ?? 0} size="small" variant="outlined" sx={{ minWidth: 32 }} />
              <Typography variant="body2" sx={{ flexGrow: 1 }}>{item.label}</Typography>
              <Typography variant="caption" color="text.secondary">{item.routePath}</Typography>
              {!item.active && <Chip label={t('fields.inactive')} size="small" color="default" />}
              <RowActionsMenu actions={[
                { label: t('common.edit'), icon: <EditIcon fontSize="small" />, onClick: () => { setEditItem(item); setDialogOpen(true); } },
                {
                  label: t('common.delete'), icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
                  onClick: () => confirm({
                    title: t('resources.menus.deleteItemTitle'), message: t('resources.menus.deleteItemMessage', { name: item.label }), confirmLabel: t('common.delete'), severity: 'error',
                    onConfirm: () => remove(item.id!),
                  }),
                },
              ]} />
            </Stack>
          ))}
        </Stack>
      )}
      <MenuItemFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditItem(null); }} editItem={editItem} defaultMenuId={menu.id} />
    </Paper>
  );
}

export default function Menus() {
  const { t } = useTranslation();
  const [expandedMenu, setExpandedMenu] = useState<Menu | null>(null);

  const columns: ColumnDef<Menu>[] = useMemo(() => [
    { field: 'name', headerName: t('fields.name'), width: 180 },
    { field: 'code', headerName: t('fields.code'), width: 140 },
    { field: 'active', headerName: t('fields.active'), width: 100, renderCell: (row) => <Chip label={row.active ? t('fields.active') : t('fields.inactive')} size="small" color={row.active ? 'success' : 'default'} /> },
  ], [t]);

  return (
    <CrudPage<Menu>
      resource="menus"
      permission="menu"
      useList={useMenus}
      useRemove={useDeleteMenu}
      columns={columns}
      rowLabel={(row) => row.name ?? ''}
      rowActions={(row) => [{ label: t('resources.menus.manageItems'), icon: <ListAltIcon fontSize="small" />, onClick: () => setExpandedMenu(row) }]}
      renderDialog={({ open, onClose, editItem }) => <MenuFormDialog open={open} onClose={onClose} editMenu={editItem} />}
    >
      {expandedMenu && <MenuItemsPanel menu={expandedMenu} />}
    </CrudPage>
  );
}
