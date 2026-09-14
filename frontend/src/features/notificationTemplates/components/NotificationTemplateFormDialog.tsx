import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { createNotificationTemplateSchema, updateNotificationTemplateSchema, type CreateNotificationTemplateFormData, type UpdateNotificationTemplateFormData } from '../schemas/notificationTemplateSchema';
import { useCreateNotificationTemplate, useUpdateNotificationTemplate } from '../hooks/useNotificationTemplates';
import LoadingButton from '@/shared/components/LoadingButton';
import type { NotificationTemplate } from '../api/notificationTemplatesApi';

interface Props { open: boolean; onClose: () => void; editTemplate?: NotificationTemplate | null; }

export default function NotificationTemplateFormDialog({ open, onClose, editTemplate }: Props) {
  const { t } = useTranslation();
  const isEdit = Boolean(editTemplate);
  const { mutateAsync: create, isPending: creating } = useCreateNotificationTemplate();
  const { mutateAsync: update, isPending: updating } = useUpdateNotificationTemplate();
  const isPending = creating || updating;

  const createForm = useForm<CreateNotificationTemplateFormData>({
    resolver: zodResolver(createNotificationTemplateSchema),
    defaultValues: { code: '', name: '', subjectTemplate: '', bodyTemplate: '', channel: 'email' },
  });
  const updateForm = useForm<UpdateNotificationTemplateFormData>({
    resolver: zodResolver(updateNotificationTemplateSchema),
    defaultValues: { name: '', subjectTemplate: '', bodyTemplate: '', channel: 'email' },
  });

  useEffect(() => {
    if (editTemplate) {
      updateForm.reset({
        name: editTemplate.name ?? '', subjectTemplate: editTemplate.subjectTemplate ?? '',
        bodyTemplate: editTemplate.bodyTemplate ?? '', channel: editTemplate.channel ?? 'email',
      });
    } else {
      createForm.reset({ code: '', name: '', subjectTemplate: '', bodyTemplate: '', channel: 'email' });
    }
  }, [editTemplate, createForm, updateForm]);

  const onSubmitCreate = async (data: CreateNotificationTemplateFormData) => { await create(data); onClose(); };
  const onSubmitUpdate = async (data: UpdateNotificationTemplateFormData) => {
    if (!editTemplate) return;
    await update({ id: editTemplate.id!, body: data });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? t('resources.notificationTemplates.edit') : t('resources.notificationTemplates.add')}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label={t('fields.name')} fullWidth {...updateForm.register('name')} error={!!updateForm.formState.errors.name} helperText={updateForm.formState.errors.name?.message} />
            <TextField label={t('fields.channel')} fullWidth {...updateForm.register('channel')} error={!!updateForm.formState.errors.channel} helperText={updateForm.formState.errors.channel?.message} placeholder={t('ui.channelExample')} />
            <TextField label={t('fields.subjectTemplate')} fullWidth {...updateForm.register('subjectTemplate')} />
            <TextField label={t('fields.bodyTemplate')} fullWidth multiline rows={4} {...updateForm.register('bodyTemplate')} error={!!updateForm.formState.errors.bodyTemplate} helperText={updateForm.formState.errors.bodyTemplate?.message} />
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label={t('fields.code')} fullWidth {...createForm.register('code')} error={!!createForm.formState.errors.code} helperText={createForm.formState.errors.code?.message} />
            <TextField label={t('fields.name')} fullWidth {...createForm.register('name')} error={!!createForm.formState.errors.name} helperText={createForm.formState.errors.name?.message} />
            <TextField label={t('fields.channel')} fullWidth {...createForm.register('channel')} error={!!createForm.formState.errors.channel} helperText={createForm.formState.errors.channel?.message} placeholder={t('ui.channelExample')} />
            <TextField label={t('fields.subjectTemplate')} fullWidth {...createForm.register('subjectTemplate')} />
            <TextField label={t('fields.bodyTemplate')} fullWidth multiline rows={4} {...createForm.register('bodyTemplate')} error={!!createForm.formState.errors.bodyTemplate} helperText={createForm.formState.errors.bodyTemplate?.message} />
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
