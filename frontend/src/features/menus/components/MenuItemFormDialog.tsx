import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem as MuiMenuItem, Stack } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { createMenuItemSchema, updateMenuItemSchema, type CreateMenuItemFormData, type UpdateMenuItemFormData } from '../schemas/menuSchema';
import { useCreateMenuItem, useUpdateMenuItem, useMenus } from '../hooks/useMenus';
import LoadingButton from '@/shared/components/LoadingButton';
import type { MenuItem } from '../api/menusApi';

interface Props { open: boolean; onClose: () => void; editItem?: MenuItem | null; defaultMenuId?: string; }

export default function MenuItemFormDialog({ open, onClose, editItem, defaultMenuId }: Props) {
  const { t } = useTranslation();
  const isEdit = Boolean(editItem);
  const { mutateAsync: create, isPending: creating } = useCreateMenuItem();
  const { mutateAsync: update, isPending: updating } = useUpdateMenuItem();
  const { data: menusPage } = useMenus({ page: 0, size: 100 });
  const isPending = creating || updating;

  const createForm = useForm<CreateMenuItemFormData>({
    resolver: zodResolver(createMenuItemSchema),
    defaultValues: { menuId: defaultMenuId ?? '', label: '', routePath: '', moduleKey: '', icon: '', sortOrder: 0 },
  });
  const updateForm = useForm<UpdateMenuItemFormData>({
    resolver: zodResolver(updateMenuItemSchema),
    defaultValues: { label: '', routePath: '', moduleKey: '', icon: '', sortOrder: 0 },
  });

  useEffect(() => {
    if (editItem) {
      updateForm.reset({
        label: editItem.label ?? '', routePath: editItem.routePath ?? '', moduleKey: editItem.moduleKey ?? '',
        icon: editItem.icon ?? '', sortOrder: editItem.sortOrder ?? 0,
      });
    } else {
      createForm.reset({ menuId: defaultMenuId ?? '', label: '', routePath: '', moduleKey: '', icon: '', sortOrder: 0 });
    }
  }, [editItem, defaultMenuId, createForm, updateForm]);

  const onSubmitCreate = async (data: CreateMenuItemFormData) => {
    await create({ ...data, parentMenuItemId: data.parentMenuItemId || undefined });
    onClose();
  };
  const onSubmitUpdate = async (data: UpdateMenuItemFormData) => {
    if (!editItem) return;
    await update({ id: editItem.id!, body: { ...data, parentMenuItemId: data.parentMenuItemId || undefined } });
    onClose();
  };

  const menus = menusPage?.items ?? [];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? t('resources.menus.editItem') : t('resources.menus.addItemTitle')}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label={t('fields.label')} fullWidth {...updateForm.register('label')} error={!!updateForm.formState.errors.label} helperText={updateForm.formState.errors.label?.message} />
            <TextField label={t('fields.routePath')} fullWidth {...updateForm.register('routePath')} />
            <TextField label={t('fields.moduleKey')} fullWidth {...updateForm.register('moduleKey')} />
            <TextField label={t('fields.icon')} fullWidth {...updateForm.register('icon')} />
            <TextField label={t('fields.sortOrder')} type="number" fullWidth {...updateForm.register('sortOrder', { valueAsNumber: true })} />
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label={t('fields.menu')} select fullWidth {...createForm.register('menuId')} error={!!createForm.formState.errors.menuId} helperText={createForm.formState.errors.menuId?.message}>
              {menus.map((m) => <MuiMenuItem key={m.id} value={m.id}>{m.name}</MuiMenuItem>)}
            </TextField>
            <TextField label={t('fields.label')} fullWidth {...createForm.register('label')} error={!!createForm.formState.errors.label} helperText={createForm.formState.errors.label?.message} />
            <TextField label={t('fields.routePath')} fullWidth {...createForm.register('routePath')} />
            <TextField label={t('fields.moduleKey')} fullWidth {...createForm.register('moduleKey')} />
            <TextField label={t('fields.icon')} fullWidth {...createForm.register('icon')} />
            <TextField label={t('fields.sortOrder')} type="number" fullWidth {...createForm.register('sortOrder', { valueAsNumber: true })} />
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
