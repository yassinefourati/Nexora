import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { createNotificationTemplateSchema, updateNotificationTemplateSchema, type CreateNotificationTemplateFormData, type UpdateNotificationTemplateFormData } from '../schemas/notificationTemplateSchema';
import { useCreateNotificationTemplate, useUpdateNotificationTemplate } from '../hooks/useNotificationTemplates';
import LoadingButton from '@/shared/components/LoadingButton';
import type { NotificationTemplate } from '../api/notificationTemplatesApi';

interface Props { open: boolean; onClose: () => void; editTemplate?: NotificationTemplate | null; }

export default function NotificationTemplateFormDialog({ open, onClose, editTemplate }: Props) {
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
      <DialogTitle>{isEdit ? 'Edit Notification Template' : 'Add Notification Template'}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Name" fullWidth {...updateForm.register('name')} error={!!updateForm.formState.errors.name} helperText={updateForm.formState.errors.name?.message} />
            <TextField label="Channel" fullWidth {...updateForm.register('channel')} error={!!updateForm.formState.errors.channel} helperText={updateForm.formState.errors.channel?.message} placeholder="email, sms, push" />
            <TextField label="Subject template" fullWidth {...updateForm.register('subjectTemplate')} />
            <TextField label="Body template" fullWidth multiline rows={4} {...updateForm.register('bodyTemplate')} error={!!updateForm.formState.errors.bodyTemplate} helperText={updateForm.formState.errors.bodyTemplate?.message} />
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Code" fullWidth {...createForm.register('code')} error={!!createForm.formState.errors.code} helperText={createForm.formState.errors.code?.message} />
            <TextField label="Name" fullWidth {...createForm.register('name')} error={!!createForm.formState.errors.name} helperText={createForm.formState.errors.name?.message} />
            <TextField label="Channel" fullWidth {...createForm.register('channel')} error={!!createForm.formState.errors.channel} helperText={createForm.formState.errors.channel?.message} placeholder="email, sms, push" />
            <TextField label="Subject template" fullWidth {...createForm.register('subjectTemplate')} />
            <TextField label="Body template" fullWidth multiline rows={4} {...createForm.register('bodyTemplate')} error={!!createForm.formState.errors.bodyTemplate} helperText={createForm.formState.errors.bodyTemplate?.message} />
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
