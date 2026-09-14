import { useMemo, useState, type ReactNode } from 'react';
import { Box, Button, Alert } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import InboxIcon from '@mui/icons-material/Inbox';
import { useTranslation } from 'react-i18next';
import type { UseQueryResult } from '@tanstack/react-query';
import PageHeader from '@/shared/components/PageHeader';
import EmptyState from '@/shared/components/EmptyState/EmptyState';
import TableSkeleton from '@/shared/components/skeletons/TableSkeleton';
import RowActionsMenu, { type RowAction } from '@/shared/components/RowActionsMenu';
import ServerDataTable, { type ServerSort } from '@/shared/components/AdvancedDataTable/ServerDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import type { ListParams, ResourcePage } from '@/core/api/useCrudResource';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import { usePermission } from '@/shared/hooks/usePermission';
import type { Resource } from '@/shared/types/roles';

/** Props every create/edit dialog receives from the page. */
export interface CrudDialogProps<T> {
  open: boolean;
  onClose: () => void;
  /** null when creating */
  editItem: T | null;
}

export interface CrudPageProps<T extends { id?: string }> {
  /** i18n namespace: `resources.<resource>.{title,description,add,deleteTitle,deleteMessage}` */
  resource: string;
  /** Backend permission resource that gates create/edit/delete; omit to show actions to every signed-in user (the API still enforces). */
  permission?: Resource;
  useList: (params: ListParams) => UseQueryResult<ResourcePage<T>>;
  useRemove?: () => { mutate: (id: string) => void };
  columns: ColumnDef<T>[];
  /** Human label of a row, used in the delete confirmation. */
  rowLabel: (row: T) => string;
  defaultSort?: ServerSort | null;
  /** Extra query params sent with every list request (filters). */
  extraParams?: ListParams;
  /** Create/edit dialog. When omitted the page is read-only. */
  renderDialog?: (props: CrudDialogProps<T>) => ReactNode;
  /** Whether rows can be edited through the dialog (default: true when a dialog is given). */
  editable?: boolean;
  /** Additional per-row actions, appended after Edit and before Delete. */
  rowActions?: (row: T, helpers: { edit: () => void }) => RowAction[];
  /** Hide the delete action for some rows (e.g. already revoked). */
  canDeleteRow?: (row: T) => boolean;
  onRowClick?: (row: T) => void;
  /** Extra buttons next to the create button. */
  headerActions?: ReactNode;
  emptyIcon?: ReactNode;
  /** i18n key for the delete action label, e.g. `common.detach` or `common.revoke` (default `common.delete`). */
  deleteActionKey?: string;
  /** Rendered below the table (details panels, secondary lists). */
  children?: ReactNode;
}

/**
 * The one list page. Every resource screen is this component plus a column
 * list and a dialog: server-driven search/sort/pagination, loading skeleton on
 * first load, error state with retry, empty state with a create shortcut,
 * permission-gated actions, confirmed deletes, and translated chrome.
 */
export default function CrudPage<T extends { id?: string }>({
  resource, permission, useList, useRemove, columns, rowLabel, defaultSort = null, extraParams,
  renderDialog, editable, rowActions, canDeleteRow, onRowClick, headerActions, emptyIcon, deleteActionKey = 'common.delete', children,
}: CrudPageProps<T>) {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<ServerSort | null>(defaultSort);
  const [search, setSearch] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editItem, setEditItem] = useState<T | null>(null);

  // Permission hooks must run unconditionally; when no resource is given every action is allowed.
  const permResource: Resource = permission ?? 'user';
  const mayWrite = usePermission(permResource, 'write');
  const mayEdit = usePermission(permResource, 'edit');
  const mayDelete = usePermission(permResource, 'delete');
  const canCreate = Boolean(renderDialog) && (permission ? mayWrite : true);
  const canEdit = Boolean(renderDialog) && (editable ?? true) && (permission ? mayEdit : true);
  const canRemove = Boolean(useRemove) && (permission ? mayDelete : true);

  const params: ListParams = {
    page, size: pageSize,
    ...(search ? { q: search } : {}),
    ...(sort ? { sort: `${sort.field},${sort.direction}` } : {}),
    ...extraParams,
  };
  const { data, isPending, isFetching, isError, refetch } = useList(params);
  const removeMutation = useRemove?.();
  const remove = removeMutation?.mutate;
  const { confirm } = useConfirmStore();

  const title = t(`resources.${resource}.title`);
  const description = t(`resources.${resource}.description`, { defaultValue: '' });

  const openCreate = () => { setEditItem(null); setDialogOpen(true); };
  const openEdit = (row: T) => { setEditItem(row); setDialogOpen(true); };

  const allColumns = useMemo<ColumnDef<T>[]>(() => {
    const hasActions = canEdit || canRemove || Boolean(rowActions);
    if (!hasActions) return columns;
    const actionsColumn: ColumnDef<T> = {
      field: 'actions', headerName: '', width: 70, sortable: false,
      renderCell: (row) => {
        const actions: RowAction[] = [];
        if (canEdit) actions.push({ label: t('common.edit'), icon: <EditIcon fontSize="small" />, onClick: () => openEdit(row) });
        if (rowActions) actions.push(...rowActions(row, { edit: () => openEdit(row) }));
        if (canRemove && remove && (canDeleteRow?.(row) ?? true)) {
          actions.push({
            label: t(deleteActionKey), icon: <DeleteIcon fontSize="small" color="error" />, color: 'error',
            onClick: () => confirm({
              title: t(`resources.${resource}.deleteTitle`, { defaultValue: t('common.deleteTitle', { item: title }) }),
              message: t(`resources.${resource}.deleteMessage`, { defaultValue: t('common.deleteMessage', { name: rowLabel(row) }), name: rowLabel(row) }),
              confirmLabel: t(deleteActionKey), severity: 'error',
              onConfirm: () => { if (row.id) remove(row.id); },
            }),
          });
        }
        return actions.length ? <RowActionsMenu actions={actions} /> : null;
      },
    };
    return [...columns, actionsColumn];
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [columns, canEdit, canRemove, rowActions, canDeleteRow, remove, confirm, t, resource, deleteActionKey]);

  const rows = data?.items ?? [];
  const firstLoad = isPending && !data;
  const isEmpty = !firstLoad && !isError && rows.length === 0 && !search;

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', gap: 2 }}>
      <PageHeader
        title={title}
        description={description || undefined}
        actions={(canCreate || headerActions) ? (
          <>
            {headerActions}
            {canCreate && (
              <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
                {t(`resources.${resource}.add`, { defaultValue: t('common.add') })}
              </Button>
            )}
          </>
        ) : undefined}
      />

      {isError ? (
        <Alert
          severity="error"
          action={<Button color="inherit" size="small" onClick={() => void refetch()}>{t('common.tryAgain')}</Button>}
        >
          {t('table.loadFailed')}
        </Alert>
      ) : firstLoad ? (
        <TableSkeleton />
      ) : isEmpty ? (
        <EmptyState
          icon={emptyIcon ?? <InboxIcon fontSize="inherit" />}
          title={t(`resources.${resource}.emptyTitle`, { defaultValue: t('common.emptyTitle', { item: title }) })}
          description={t(`resources.${resource}.emptyDescription`, { defaultValue: t('common.emptyDescription') })}
          action={canCreate ? { label: t(`resources.${resource}.add`, { defaultValue: t('common.add') }), onClick: openCreate } : undefined}
        />
      ) : (
        <ServerDataTable
          title={title}
          rows={rows}
          columns={allColumns}
          pagination={data?.pagination}
          isLoading={isFetching}
          onRefetch={() => void refetch()}
          page={page}
          pageSize={pageSize}
          onPageChange={setPage}
          onPageSizeChange={setPageSize}
          sort={sort}
          onSortChange={(s) => { setSort(s); setPage(0); }}
          search={search}
          onSearchChange={(s) => { setSearch(s); setPage(0); }}
          rowKey={(row) => row.id ?? rowLabel(row)}
          onRowClick={onRowClick}
        />
      )}

      {children}

      {renderDialog?.({ open: dialogOpen, onClose: () => { setDialogOpen(false); setEditItem(null); }, editItem })}
    </Box>
  );
}
