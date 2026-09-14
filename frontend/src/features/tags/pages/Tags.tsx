import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useTags, useDeleteTag } from '../hooks/useTags';
import TagFormDialog from '../components/TagFormDialog';
import type { Tag } from '../api/tagsApi';
import { Chip } from '@mui/material';

export default function Tags() {
  const { t } = useTranslation();
  const columns: ColumnDef<Tag>[] = useMemo(() => [
    { field: 'name', headerName: t('fields.name'), width: 200, renderCell: (row) => <Chip label={row.name} size="small" sx={row.color ? { bgcolor: row.color, color: '#fff' } : undefined} /> },
    { field: 'color', headerName: t('fields.color'), width: 120 },
  ], [t]);

  return (
    <CrudPage<Tag>
      resource="tags"
      useList={useTags}
      useRemove={useDeleteTag}
      columns={columns}
      rowLabel={(row) => row.name ?? ''}
      renderDialog={({ open, onClose, editItem }) => <TagFormDialog open={open} onClose={onClose} editTag={editItem} />}
    ></CrudPage>
  );
}
