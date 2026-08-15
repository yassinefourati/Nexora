import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { entityTagSchema, type EntityTagFormData } from '../schemas/entityTagSchema';
import { useCreateEntityTag } from '../hooks/useEntityTags';
import { useTags } from '@/features/tags/hooks/useTags';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

export default function EntityTagFormDialog({ open, onClose }: Props) {
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
      <DialogTitle>Attach Tag to Entity</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Tag" select fullWidth {...register('tagId')} error={!!errors.tagId} helperText={errors.tagId?.message}>
            {tags.map((t) => <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>)}
          </TextField>
          <TextField label="Entity type" fullWidth {...register('entityType')} error={!!errors.entityType} helperText={errors.entityType?.message} placeholder="e.g. user, organization" />
          <TextField label="Entity ID" fullWidth {...register('entityId')} error={!!errors.entityId} helperText={errors.entityId?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>Attach</LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
