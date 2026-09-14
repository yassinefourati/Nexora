import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { tagSchema, type TagFormData } from '../schemas/tagSchema';
import { useCreateTag, useUpdateTag } from '../hooks/useTags';
import LoadingButton from '@/shared/components/LoadingButton';
import type { Tag } from '../api/tagsApi';

interface Props { open: boolean; onClose: () => void; editTag?: Tag | null; }

export default function TagFormDialog({ open, onClose, editTag }: Props) {
  const { t } = useTranslation();
  const isEdit = Boolean(editTag);
  const { mutateAsync: create, isPending: creating } = useCreateTag();
  const { mutateAsync: update, isPending: updating } = useUpdateTag();
  const isPending = creating || updating;

  const { register, handleSubmit, reset, formState: { errors } } = useForm<TagFormData>({
    resolver: zodResolver(tagSchema),
    defaultValues: { name: '', color: '' },
  });

  useEffect(() => {
    reset(editTag ? { name: editTag.name ?? '', color: editTag.color ?? '' } : { name: '', color: '' });
  }, [editTag, reset]);

  const onSubmit = async (data: TagFormData) => {
    if (isEdit && editTag) {
      await update({ id: editTag.id!, body: data });
    } else {
      await create(data);
    }
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? t('resources.tags.edit') : t('resources.tags.add')}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label={t('fields.name')} fullWidth {...register('name')} error={!!errors.name} helperText={errors.name?.message} />
          <TextField label={t('fields.color')} fullWidth {...register('color')} placeholder={t('fields.colorPlaceholder')} error={!!errors.color} helperText={errors.color?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>{t('common.cancel')}</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          {isEdit ? t('common.save') : t('common.create')}
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
