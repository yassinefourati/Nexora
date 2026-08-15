import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack, FormControlLabel, Checkbox } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { createAppModuleSchema, updateAppModuleSchema, type CreateAppModuleFormData, type UpdateAppModuleFormData } from '../schemas/appModuleSchema';
import { useCreateAppModule, useUpdateAppModule } from '../hooks/useAppModules';
import LoadingButton from '@/shared/components/LoadingButton';
import type { AppModule } from '../api/appModulesApi';

interface Props { open: boolean; onClose: () => void; editModule?: AppModule | null; }

export default function AppModuleFormDialog({ open, onClose, editModule }: Props) {
  const isEdit = Boolean(editModule);
  const { mutateAsync: create, isPending: creating } = useCreateAppModule();
  const { mutateAsync: update, isPending: updating } = useUpdateAppModule();
  const isPending = creating || updating;

  const createForm = useForm<CreateAppModuleFormData>({
    resolver: zodResolver(createAppModuleSchema),
    defaultValues: { key: '', name: '', description: '', active: true },
  });
  const updateForm = useForm<UpdateAppModuleFormData>({
    resolver: zodResolver(updateAppModuleSchema),
    defaultValues: { name: '', description: '', active: true },
  });

  useEffect(() => {
    if (editModule) {
      updateForm.reset({ name: editModule.name ?? '', description: editModule.description ?? '', active: editModule.active ?? true });
    } else {
      createForm.reset({ key: '', name: '', description: '', active: true });
    }
  }, [editModule, createForm, updateForm]);

  const onSubmitCreate = async (data: CreateAppModuleFormData) => { await create(data); onClose(); };
  const onSubmitUpdate = async (data: UpdateAppModuleFormData) => {
    if (!editModule) return;
    await update({ id: editModule.id!, body: data });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Edit App Module' : 'Add App Module'}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Name" fullWidth {...updateForm.register('name')} error={!!updateForm.formState.errors.name} helperText={updateForm.formState.errors.name?.message} />
            <TextField label="Description" fullWidth multiline rows={2} {...updateForm.register('description')} />
            <Controller name="active" control={updateForm.control} render={({ field }) => (
              <FormControlLabel control={<Checkbox checked={field.value} onChange={field.onChange} />} label="Active" />
            )} />
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Key" fullWidth {...createForm.register('key')} error={!!createForm.formState.errors.key} helperText={createForm.formState.errors.key?.message} />
            <TextField label="Name" fullWidth {...createForm.register('name')} error={!!createForm.formState.errors.name} helperText={createForm.formState.errors.name?.message} />
            <TextField label="Description" fullWidth multiline rows={2} {...createForm.register('description')} />
            <Controller name="active" control={createForm.control} render={({ field }) => (
              <FormControlLabel control={<Checkbox checked={field.value} onChange={field.onChange} />} label="Active" />
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
