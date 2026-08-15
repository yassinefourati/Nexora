import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack, FormControlLabel, Checkbox } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { createSettingSchema, updateSettingSchema, type CreateSettingFormData, type UpdateSettingFormData } from '../schemas/settingSchema';
import { useCreateSetting, useUpdateSetting } from '../hooks/useSettings';
import { SETTING_SCOPES } from '../api/settingsApi';
import { useOrganizations } from '@/features/organizations/hooks/useOrganizations';
import LoadingButton from '@/shared/components/LoadingButton';
import type { Setting } from '../api/settingsApi';

interface Props { open: boolean; onClose: () => void; editSetting?: Setting | null; }

export default function SettingFormDialog({ open, onClose, editSetting }: Props) {
  const isEdit = Boolean(editSetting);
  const { mutateAsync: create, isPending: creating } = useCreateSetting();
  const { mutateAsync: update, isPending: updating } = useUpdateSetting();
  const { data: orgsPage } = useOrganizations({ page: 0, size: 100 });
  const isPending = creating || updating;

  const createForm = useForm<CreateSettingFormData>({
    resolver: zodResolver(createSettingSchema),
    defaultValues: { scope: 'global', organizationId: '', key: '', value: '', description: '', editable: true },
  });
  const updateForm = useForm<UpdateSettingFormData>({
    resolver: zodResolver(updateSettingSchema),
    defaultValues: { value: '', description: '', editable: true },
  });

  useEffect(() => {
    if (editSetting) {
      updateForm.reset({ value: editSetting.value ?? '', description: editSetting.description ?? '', editable: editSetting.editable ?? true });
    } else {
      createForm.reset({ scope: 'global', organizationId: '', key: '', value: '', description: '', editable: true });
    }
  }, [editSetting, createForm, updateForm]);

  const scope = createForm.watch('scope');

  const onSubmitCreate = async (data: CreateSettingFormData) => {
    await create({ ...data, organizationId: data.organizationId || undefined });
    onClose();
  };
  const onSubmitUpdate = async (data: UpdateSettingFormData) => {
    if (!editSetting) return;
    await update({ id: editSetting.id!, body: data });
    onClose();
  };

  const orgs = orgsPage?.items ?? [];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? `Edit Setting: ${editSetting?.key}` : 'Add Setting'}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Value" fullWidth multiline rows={3} {...updateForm.register('value')} />
            <TextField label="Description" fullWidth {...updateForm.register('description')} />
            <Controller name="editable" control={updateForm.control} render={({ field }) => (
              <FormControlLabel control={<Checkbox checked={field.value} onChange={field.onChange} />} label="Editable" />
            )} />
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Scope" select fullWidth {...createForm.register('scope')}>
              {SETTING_SCOPES.map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
            </TextField>
            {scope === 'organization' && (
              <TextField label="Organization" select fullWidth {...createForm.register('organizationId')} error={!!createForm.formState.errors.organizationId} helperText={createForm.formState.errors.organizationId?.message}>
                {orgs.map((o) => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
              </TextField>
            )}
            <TextField label="Key" fullWidth {...createForm.register('key')} error={!!createForm.formState.errors.key} helperText={createForm.formState.errors.key?.message} placeholder="e.g. app.name" />
            <TextField label="Value" fullWidth multiline rows={3} {...createForm.register('value')} helperText="Stored as JSON — wrap plain strings in quotes" />
            <TextField label="Description" fullWidth {...createForm.register('description')} />
            <Controller name="editable" control={createForm.control} render={({ field }) => (
              <FormControlLabel control={<Checkbox checked={field.value} onChange={field.onChange} />} label="Editable" />
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
