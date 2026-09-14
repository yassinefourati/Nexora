import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useMetadata, useDeleteMetadata } from '../hooks/useMetadata';
import MetadataFormDialog from '../components/MetadataFormDialog';
import type { MetadataKv } from '../api/metadataApi';
import { Chip } from '@mui/material';

export default function Metadata() {
  const { t } = useTranslation();
  const columns: ColumnDef<MetadataKv>[] = useMemo(() => [
    { field: 'entityType', headerName: t('fields.entityType'), width: 140, renderCell: (row) => <Chip label={row.entityType} size="small" variant="outlined" /> },
    { field: 'entityId', headerName: t('fields.entityId'), width: 220 },
    { field: 'key', headerName: t('fields.key'), width: 160 },
    { field: 'value', headerName: t('fields.value'), width: 220 },
  ], [t]);

  return (
    <CrudPage<MetadataKv>
      resource="metadata"
      useList={useMetadata}
      useRemove={useDeleteMetadata}
      columns={columns}
      rowLabel={(row) => row.key ?? ''}
      renderDialog={({ open, onClose, editItem }) => <MetadataFormDialog open={open} onClose={onClose} editEntry={editItem} />}
    ></CrudPage>
  );
}
