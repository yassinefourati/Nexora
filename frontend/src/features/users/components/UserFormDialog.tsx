import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack, FormControlLabel, Checkbox } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { createUserSchema, updateUserSchema, type CreateUserFormData, type UpdateUserFormData } from '../schemas/userSchema';
import { useCreateUser, useUpdateUser } from '../hooks/useUsers';
import LoadingButton from '@/shared/components/LoadingButton';
import type { User } from '../api/usersApi';

interface Props { open: boolean; onClose: () => void; editUser?: User | null; }

const STATUS_OPTIONS = ['active', 'disabled', 'locked'];

export default function UserFormDialog({ open, onClose, editUser }: Props) {
  const { t } = useTranslation();
  const isEdit = Boolean(editUser);
  const { mutateAsync: create, isPending: creating } = useCreateUser();
  const { mutateAsync: update, isPending: updating } = useUpdateUser();
  const isPending = creating || updating;

  const createForm = useForm<CreateUserFormData>({
    resolver: zodResolver(createUserSchema),
    defaultValues: { username: '', email: '', firstName: '', lastName: '', status: 'active', superuser: false },
  });
  const updateForm = useForm<UpdateUserFormData>({
    resolver: zodResolver(updateUserSchema),
    defaultValues: { email: '', firstName: '', lastName: '', status: 'active', superuser: false },
  });

  useEffect(() => {
    if (editUser) {
      updateForm.reset({
        email: editUser.email ?? '',
        firstName: editUser.firstName ?? '',
        lastName: editUser.lastName ?? '',
        status: editUser.status ?? 'active',
        superuser: editUser.superuser ?? false,
        version: editUser.version ?? undefined,
      });
    } else {
      createForm.reset({ username: '', email: '', firstName: '', lastName: '', status: 'active', superuser: false });
    }
  }, [editUser, createForm, updateForm]);

  const onSubmitCreate = async (data: CreateUserFormData) => {
    await create(data);
    onClose();
  };

  const onSubmitUpdate = async (data: UpdateUserFormData) => {
    if (!editUser) return;
    await update({ id: editUser.id!, body: data });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? t('resources.users.edit') : t('resources.users.add')}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label={t('fields.firstName')} fullWidth {...updateForm.register('firstName')}
              error={!!updateForm.formState.errors.firstName} helperText={updateForm.formState.errors.firstName?.message} />
            <TextField label={t('fields.lastName')} fullWidth {...updateForm.register('lastName')}
              error={!!updateForm.formState.errors.lastName} helperText={updateForm.formState.errors.lastName?.message} />
            <TextField label={t('fields.email')} fullWidth {...updateForm.register('email')}
              error={!!updateForm.formState.errors.email} helperText={updateForm.formState.errors.email?.message} />
            <TextField label={t('fields.status')} select fullWidth {...updateForm.register('status')}
              error={!!updateForm.formState.errors.status} helperText={updateForm.formState.errors.status?.message}>
              {STATUS_OPTIONS.map((s) => <MenuItem key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</MenuItem>)}
            </TextField>
            <FormControlLabel control={<Checkbox {...updateForm.register('superuser')} />} label={t('fields.superuser')} />
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label={t('fields.username')} fullWidth {...createForm.register('username')}
              error={!!createForm.formState.errors.username} helperText={createForm.formState.errors.username?.message} />
            <TextField label={t('fields.firstName')} fullWidth {...createForm.register('firstName')}
              error={!!createForm.formState.errors.firstName} helperText={createForm.formState.errors.firstName?.message} />
            <TextField label={t('fields.lastName')} fullWidth {...createForm.register('lastName')}
              error={!!createForm.formState.errors.lastName} helperText={createForm.formState.errors.lastName?.message} />
            <TextField label={t('fields.email')} fullWidth {...createForm.register('email')}
              error={!!createForm.formState.errors.email} helperText={createForm.formState.errors.email?.message} />
            <FormControlLabel control={<Checkbox {...createForm.register('superuser')} />} label={t('fields.superuser')} />
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
