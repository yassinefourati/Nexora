import { useState, useMemo } from 'react';
import { Box, Button, Chip, Paper, Typography, Stack } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import ListAltIcon from '@mui/icons-material/ListAlt';
import { useMenus, useDeleteMenu, useMenuItems, useDeleteMenuItem } from '../hooks/useMenus';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import PageHeader from '@/shared/components/PageHeader';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import MenuFormDialog from '../components/MenuFormDialog';
import MenuItemFormDialog from '../components/MenuItemFormDialog';
import type { Menu, MenuItem } from '../api/menusApi';

function MenuItemsPanel({ menu }: { menu: Menu }) {
  const { data, isLoading } = useMenuItems({ menuId: menu.id, page: 0, size: 100 });
  const { mutate: remove } = useDeleteMenuItem();
  const { confirm } = useConfirmStore();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editItem, setEditItem] = useState<MenuItem | null>(null);
  const items = data?.items ?? [];

  return (
    <Paper variant="outlined" sx={{ p: 2, borderRadius: 2 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={1.5}>
        <Typography variant="subtitle2" fontWeight={700}>Items in "{menu.name}"</Typography>
        <Button size="small" startIcon={<AddIcon fontSize="small" />} onClick={() => { setEditItem(null); setDialogOpen(true); }}>Add item</Button>
      </Stack>
      {isLoading ? (
        <Typography variant="body2" color="text.secondary">Loading…</Typography>
      ) : items.length === 0 ? (
        <Typography variant="body2" color="text.secondary">No items yet.</Typography>
      ) : (
        <Stack spacing={1}>
          {items.map((item) => (
            <Stack key={item.id} direction="row" alignItems="center" spacing={1} sx={{ py: 0.5 }}>
              <Chip label={item.sortOrder ?? 0} size="small" variant="outlined" sx={{ minWidth: 32 }} />
              <Typography variant="body2" sx={{ flexGrow: 1 }}>{item.label}</Typography>
              <Typography variant="caption" color="text.secondary">{item.routePath}</Typography>
              {!item.active && <Chip label="Inactive" size="small" color="default" />}
              <RowActionsMenu actions={[
                { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditItem(item); setDialogOpen(true); } },
                {
                  label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
                  onClick: () => confirm({
                    title: 'Delete menu item', message: `Delete "${item.label}"?`, confirmLabel: 'Delete', severity: 'error',
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
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(null);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editMenu, setEditMenu] = useState<Menu | null>(null);
  const [expandedMenu, setExpandedMenu] = useState<Menu | null>(null);

  const { data, isLoading, isError, refetch } = useMenus({
    page, size: pageSize,
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
  });
  const { mutate: remove } = useDeleteMenu();
  const { confirm } = useConfirmStore();

  const rows = useMemo(() => {
    const items = data?.items ?? [];
    if (!search) return items;
    const term = search.toLowerCase();
    return items.filter((m) => m.name?.toLowerCase().includes(term) || m.code?.toLowerCase().includes(term));
  }, [data, search]);

  const columns: ColumnDef<Menu>[] = useMemo(() => [
    { field: 'name', headerName: 'Name', width: 180 },
    { field: 'code', headerName: 'Code', width: 140 },
    { field: 'active', headerName: 'Active', width: 100, renderCell: (row) => <Chip label={row.active ? 'Active' : 'Inactive'} size="small" color={row.active ? 'success' : 'default'} /> },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: (row) => (
        <RowActionsMenu
          actions={[
            { label: 'Manage items', icon: <ListAltIcon fontSize="small" />, onClick: () => setExpandedMenu(row) },
            { label: 'Edit', icon: <EditIcon fontSize="small" />, onClick: () => { setEditMenu(row); setDialogOpen(true); } },
            {
              label: 'Delete', icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
              onClick: () => confirm({
                title: 'Delete menu', message: `Delete menu "${row.name}"? Its items will also be removed.`, confirmLabel: 'Delete', severity: 'error',
                onConfirm: () => remove(row.id!),
              }),
            },
          ]}
        />
      ),
    },
  ], [confirm, remove]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', gap: 2 }}>
      <PageHeader
        title="Menus"
        description="Manage navigation menus and their items."
        actions={<Button variant="contained" startIcon={<AddIcon />} onClick={() => { setEditMenu(null); setDialogOpen(true); }}>Add Menu</Button>}
      />
      <ServerDataTable
        title="Menus"
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
      {expandedMenu && <MenuItemsPanel menu={expandedMenu} />}
      <MenuFormDialog open={dialogOpen} onClose={() => { setDialogOpen(false); setEditMenu(null); }} editMenu={editMenu} />
    </Box>
  );
}
