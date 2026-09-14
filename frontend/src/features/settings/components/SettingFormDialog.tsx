import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack, FormControlLabel, Checkbox } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useForm, useWatch, Controller } from 'react-hook-form';
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
  const { t } = useTranslation();
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

  const scope = useWatch({ control: createForm.control, name: 'scope' });

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
      <DialogTitle>{isEdit ? t('resources.settings.edit', { key: editSetting?.key }) : t('resources.settings.add')}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label={t('fields.value')} fullWidth multiline rows={3} {...updateForm.register('value')} />
            <TextField label={t('fields.description')} fullWidth {...updateForm.register('description')} />
            <Controller name="editable" control={updateForm.control} render={({ field }) => (
              <FormControlLabel control={<Checkbox checked={field.value} onChange={field.onChange} />} label={t('fields.editable')} />
            )} />
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label={t('fields.scope')} select fullWidth {...createForm.register('scope')}>
              {SETTING_SCOPES.map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
            </TextField>
            {scope === 'organization' && (
              <TextField label={t('fields.organization')} select fullWidth {...createForm.register('organizationId')} error={!!createForm.formState.errors.organizationId} helperText={createForm.formState.errors.organizationId?.message}>
                {orgs.map((o) => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
              </TextField>
            )}
            <TextField label={t('fields.key')} fullWidth {...createForm.register('key')} error={!!createForm.formState.errors.key} helperText={createForm.formState.errors.key?.message} placeholder={t('ui.settingKeyExample')} />
            <TextField label={t('fields.value')} fullWidth multiline rows={3} {...createForm.register('value')} helperText={t('fields.valueJsonHelp')} />
            <TextField label={t('fields.description')} fullWidth {...createForm.register('description')} />
            <Controller name="editable" control={createForm.control} render={({ field }) => (
              <FormControlLabel control={<Checkbox checked={field.value} onChange={field.onChange} />} label={t('fields.editable')} />
            )} />
          </Stack>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>{t('common.cancel')}</Button>
        <LoadingButton
          variant="contained"
          onClick={isEdit ? updateForm.handleSubmit(onSubmitUpdate) : createForm.handleSubmit(onSubmitCreate)}
          loading={isPending}
        >
          {isEdit ? t('common.save') : t('common.create')}
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
