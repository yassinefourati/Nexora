import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { createMenuSchema, updateMenuSchema, type CreateMenuFormData, type UpdateMenuFormData } from '../schemas/menuSchema';
import { useCreateMenu, useUpdateMenu } from '../hooks/useMenus';
import LoadingButton from '@/shared/components/LoadingButton';
import type { Menu } from '../api/menusApi';

interface Props { open: boolean; onClose: () => void; editMenu?: Menu | null; }

export default function MenuFormDialog({ open, onClose, editMenu }: Props) {
  const { t } = useTranslation();
  const isEdit = Boolean(editMenu);
  const { mutateAsync: create, isPending: creating } = useCreateMenu();
  const { mutateAsync: update, isPending: updating } = useUpdateMenu();
  const isPending = creating || updating;

  const createForm = useForm<CreateMenuFormData>({ resolver: zodResolver(createMenuSchema), defaultValues: { name: '', code: '', description: '' } });
  const updateForm = useForm<UpdateMenuFormData>({ resolver: zodResolver(updateMenuSchema), defaultValues: { name: '', description: '' } });

  useEffect(() => {
    if (editMenu) {
      updateForm.reset({ name: editMenu.name ?? '', description: editMenu.description ?? '' });
    } else {
      createForm.reset({ name: '', code: '', description: '' });
    }
  }, [editMenu, createForm, updateForm]);

  const onSubmitCreate = async (data: CreateMenuFormData) => { await create(data); onClose(); };
  const onSubmitUpdate = async (data: UpdateMenuFormData) => {
    if (!editMenu) return;
    await update({ id: editMenu.id!, body: data });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? t('resources.menus.edit') : t('resources.menus.add')}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label={t('fields.name')} fullWidth {...updateForm.register('name')} error={!!updateForm.formState.errors.name} helperText={updateForm.formState.errors.name?.message} />
            <TextField label={t('fields.description')} fullWidth multiline rows={2} {...updateForm.register('description')} />
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label={t('fields.name')} fullWidth {...createForm.register('name')} error={!!createForm.formState.errors.name} helperText={createForm.formState.errors.name?.message} />
            <TextField label={t('fields.code')} fullWidth {...createForm.register('code')} error={!!createForm.formState.errors.code} helperText={createForm.formState.errors.code?.message} />
            <TextField label={t('fields.description')} fullWidth multiline rows={2} {...createForm.register('description')} />
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
