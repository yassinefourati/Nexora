import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { createMetadataSchema, updateMetadataSchema, type CreateMetadataFormData, type UpdateMetadataFormData } from '../schemas/metadataSchema';
import { useCreateMetadata, useUpdateMetadata } from '../hooks/useMetadata';
import LoadingButton from '@/shared/components/LoadingButton';
import type { MetadataKv } from '../api/metadataApi';

interface Props { open: boolean; onClose: () => void; editEntry?: MetadataKv | null; }

export default function MetadataFormDialog({ open, onClose, editEntry }: Props) {
  const isEdit = Boolean(editEntry);
  const { mutateAsync: create, isPending: creating } = useCreateMetadata();
  const { mutateAsync: update, isPending: updating } = useUpdateMetadata();
  const isPending = creating || updating;

  const createForm = useForm<CreateMetadataFormData>({
    resolver: zodResolver(createMetadataSchema),
    defaultValues: { entityType: '', entityId: '', key: '', value: '' },
  });
  const updateForm = useForm<UpdateMetadataFormData>({
    resolver: zodResolver(updateMetadataSchema),
    defaultValues: { value: '' },
  });

  useEffect(() => {
    if (editEntry) {
      updateForm.reset({ value: editEntry.value ?? '' });
    } else {
      createForm.reset({ entityType: '', entityId: '', key: '', value: '' });
    }
  }, [editEntry, createForm, updateForm]);

  const onSubmitCreate = async (data: CreateMetadataFormData) => { await create(data); onClose(); };
  const onSubmitUpdate = async (data: UpdateMetadataFormData) => {
    if (!editEntry) return;
    await update({ id: editEntry.id!, body: data });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? `Edit Metadata: ${editEntry?.key}` : 'Add Metadata'}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Value" fullWidth multiline rows={3} {...updateForm.register('value')} error={!!updateForm.formState.errors.value} helperText={updateForm.formState.errors.value?.message} />
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Entity type" fullWidth {...createForm.register('entityType')} error={!!createForm.formState.errors.entityType} helperText={createForm.formState.errors.entityType?.message} placeholder="e.g. user, organization" />
            <TextField label="Entity ID" fullWidth {...createForm.register('entityId')} error={!!createForm.formState.errors.entityId} helperText={createForm.formState.errors.entityId?.message} />
            <TextField label="Key" fullWidth {...createForm.register('key')} error={!!createForm.formState.errors.key} helperText={createForm.formState.errors.key?.message} />
            <TextField label="Value" fullWidth multiline rows={3} {...createForm.register('value')} error={!!createForm.formState.errors.value} helperText={createForm.formState.errors.value?.message} />
          </Stack>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton
          variant="contained"
          onClick={isEdit ? updateForm.handleSubmit(onSubmitUpdate) : createForm.handleSubmit(onSubmitCreate)}
          loading={isPending}
        >
          {isEdit ? 'Save' : 'Create'}
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
