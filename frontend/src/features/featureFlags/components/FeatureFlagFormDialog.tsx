import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack, FormControlLabel, Checkbox } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { createFeatureFlagSchema, updateFeatureFlagSchema, type CreateFeatureFlagFormData, type UpdateFeatureFlagFormData } from '../schemas/featureFlagSchema';
import { useCreateFeatureFlag, useUpdateFeatureFlag } from '../hooks/useFeatureFlags';
import { useOrganizations } from '@/features/organizations/hooks/useOrganizations';
import LoadingButton from '@/shared/components/LoadingButton';
import type { FeatureFlag } from '../api/featureFlagsApi';

interface Props { open: boolean; onClose: () => void; editFlag?: FeatureFlag | null; }

export default function FeatureFlagFormDialog({ open, onClose, editFlag }: Props) {
  const isEdit = Boolean(editFlag);
  const { mutateAsync: create, isPending: creating } = useCreateFeatureFlag();
  const { mutateAsync: update, isPending: updating } = useUpdateFeatureFlag();
  const { data: orgsPage } = useOrganizations({ page: 0, size: 100 });
  const isPending = creating || updating;

  const createForm = useForm<CreateFeatureFlagFormData>({
    resolver: zodResolver(createFeatureFlagSchema),
    defaultValues: { key: '', name: '', description: '', organizationId: '', enabled: false },
  });
  const updateForm = useForm<UpdateFeatureFlagFormData>({
    resolver: zodResolver(updateFeatureFlagSchema),
    defaultValues: { name: '', description: '', enabled: false },
  });

  useEffect(() => {
    if (editFlag) {
      updateForm.reset({ name: editFlag.name ?? '', description: editFlag.description ?? '', enabled: editFlag.enabled ?? false });
    } else {
      createForm.reset({ key: '', name: '', description: '', organizationId: '', enabled: false });
    }
  }, [editFlag, createForm, updateForm]);

  const onSubmitCreate = async (data: CreateFeatureFlagFormData) => {
    await create({ ...data, organizationId: data.organizationId || undefined });
    onClose();
  };
  const onSubmitUpdate = async (data: UpdateFeatureFlagFormData) => {
    if (!editFlag) return;
    await update({ id: editFlag.id!, body: data });
    onClose();
  };

  const orgs = orgsPage?.items ?? [];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Edit Feature Flag' : 'Add Feature Flag'}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Name" fullWidth {...updateForm.register('name')} error={!!updateForm.formState.errors.name} helperText={updateForm.formState.errors.name?.message} />
            <TextField label="Description" fullWidth multiline rows={2} {...updateForm.register('description')} />
            <Controller name="enabled" control={updateForm.control} render={({ field }) => (
              <FormControlLabel control={<Checkbox checked={field.value} onChange={field.onChange} />} label="Enabled" />
            )} />
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Key" fullWidth {...createForm.register('key')} error={!!createForm.formState.errors.key} helperText={createForm.formState.errors.key?.message} />
            <TextField label="Name" fullWidth {...createForm.register('name')} error={!!createForm.formState.errors.name} helperText={createForm.formState.errors.name?.message} />
            <TextField label="Description" fullWidth multiline rows={2} {...createForm.register('description')} />
            <TextField label="Organization (optional)" select fullWidth {...createForm.register('organizationId')}>
              <MenuItem value="">Global (all organizations)</MenuItem>
              {orgs.map((o) => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
            </TextField>
            <Controller name="enabled" control={createForm.control} render={({ field }) => (
              <FormControlLabel control={<Checkbox checked={field.value} onChange={field.onChange} />} label="Enabled" />
            )} />
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
