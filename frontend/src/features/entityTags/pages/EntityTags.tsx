import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import CrudPage from '@/shared/components/crud/CrudPage';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';
import { useEntityTags, useDeleteEntityTag } from '../hooks/useEntityTags';
import EntityTagFormDialog from '../components/EntityTagFormDialog';
import type { EntityTag } from '../api/entityTagsApi';
import { Chip } from '@mui/material';
import { useTags } from '@/features/tags/hooks/useTags';

export default function EntityTags() {
  const { t } = useTranslation();
  const { data: tagsPage } = useTags({ page: 0, size: 200 });
  const tagName = useMemo(() => { const m = new Map((tagsPage?.items ?? []).map((tg) => [tg.id, tg.name])); return (id?: string) => (id && m.get(id)) || id || t('common.dash'); }, [tagsPage, t]);
  const columns: ColumnDef<EntityTag>[] = useMemo(() => [
    { field: 'tagId', headerName: t('fields.tag'), width: 160, renderCell: (row) => <Chip label={tagName(row.tagId)} size="small" /> },
    { field: 'entityType', headerName: t('fields.entityType'), width: 160 },
    { field: 'entityId', headerName: t('fields.entityId'), width: 220 },
  ], [t, tagName]);

  return (
    <CrudPage<EntityTag>
      resource="entityTags"
      useList={useEntityTags}
      useRemove={useDeleteEntityTag}
      columns={columns}
      rowLabel={(row) => `${tagName(row.tagId)} / ${row.entityType ?? ''}`}
      editable={false}
      deleteActionKey="common.detach"
      renderDialog={({ open, onClose }) => <EntityTagFormDialog open={open} onClose={onClose} />}
    ></CrudPage>
  );
}
