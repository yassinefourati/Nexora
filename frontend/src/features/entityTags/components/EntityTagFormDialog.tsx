import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { entityTagSchema, type EntityTagFormData } from '../schemas/entityTagSchema';
import { useCreateEntityTag } from '../hooks/useEntityTags';
import { useTags } from '@/features/tags/hooks/useTags';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

export default function EntityTagFormDialog({ open, onClose }: Props) {
  const { t } = useTranslation();
  const { mutateAsync: create, isPending } = useCreateEntityTag();
  const { data: tagsPage } = useTags({ page: 0, size: 100 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<EntityTagFormData>({
    resolver: zodResolver(entityTagSchema),
    defaultValues: { tagId: '', entityType: '', entityId: '' },
  });

  const onSubmit = async (data: EntityTagFormData) => {
    await create(data);
    reset();
    onClose();
  };

  const tags = tagsPage?.items ?? [];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{t('ui.attachTagToEntity')}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label={t('fields.tag')} select fullWidth {...register('tagId')} error={!!errors.tagId} helperText={errors.tagId?.message}>
            {tags.map((t) => <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>)}
          </TextField>
          <TextField label={t('fields.entityType')} fullWidth {...register('entityType')} error={!!errors.entityType} helperText={errors.entityType?.message} placeholder={t('ui.entityTypeExample')} />
          <TextField label={t('fields.entityId')} fullWidth {...register('entityId')} error={!!errors.entityId} helperText={errors.entityId?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>{t('common.cancel')}</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>{t('common.attach')}</LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
