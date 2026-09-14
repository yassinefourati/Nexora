import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useNotificationTemplates, useDeleteNotificationTemplate } from '../hooks/useNotificationTemplates';
import NotificationTemplateFormDialog from '../components/NotificationTemplateFormDialog';
import type { NotificationTemplate } from '../api/notificationTemplatesApi';
import { Chip } from '@mui/material';

export default function NotificationTemplates() {
  const { t } = useTranslation();
  const columns: ColumnDef<NotificationTemplate>[] = useMemo(() => [
    { field: 'code', headerName: t('fields.code'), width: 160 },
    { field: 'name', headerName: t('fields.name'), width: 200 },
    { field: 'channel', headerName: t('fields.channel'), width: 110, renderCell: (row) => <Chip label={row.channel} size="small" variant="outlined" /> },
    { field: 'subjectTemplate', headerName: t('fields.subjectTemplate'), width: 260 },
  ], [t]);

  return (
    <CrudPage<NotificationTemplate>
      resource="notificationTemplates"
      useList={useNotificationTemplates}
      useRemove={useDeleteNotificationTemplate}
      columns={columns}
      rowLabel={(row) => row.name ?? ''}
      renderDialog={({ open, onClose, editItem }) => <NotificationTemplateFormDialog open={open} onClose={onClose} editTemplate={editItem} />}
    ></CrudPage>
  );
}
