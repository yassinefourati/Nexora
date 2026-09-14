import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { menuPermissionSchema, type MenuPermissionFormData } from '../schemas/menuPermissionSchema';
import { useCreateMenuPermission } from '../hooks/useMenuPermissions';
import { useMenuItems } from '@/features/menus/hooks/useMenus';
import { usePermissionsList } from '@/features/permissions/hooks/usePermissions';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

export default function MenuPermissionFormDialog({ open, onClose }: Props) {
  const { t } = useTranslation();
  const { mutateAsync: create, isPending } = useCreateMenuPermission();
  const { data: menuItemsPage } = useMenuItems({ page: 0, size: 200 });
  const { data: permissionsPage } = usePermissionsList({ page: 0, size: 200 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<MenuPermissionFormData>({
    resolver: zodResolver(menuPermissionSchema),
    defaultValues: { menuItemId: '', permissionId: '' },
  });

  const onSubmit = async (data: MenuPermissionFormData) => {
    await create(data);
    reset();
    onClose();
  };

  const menuItems = menuItemsPage?.items ?? [];
  const permissions = permissionsPage?.items ?? [];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{t('ui.grantMenuPermission')}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label={t('fields.menuItem')} select fullWidth {...register('menuItemId')} error={!!errors.menuItemId} helperText={errors.menuItemId?.message}>
            {menuItems.map((mi) => <MenuItem key={mi.id} value={mi.id}>{mi.label}</MenuItem>)}
          </TextField>
          <TextField label={t('fields.permission')} select fullWidth {...register('permissionId')} error={!!errors.permissionId} helperText={errors.permissionId?.message}>
            {permissions.map((p) => <MenuItem key={p.id} value={p.id}>{p.code}</MenuItem>)}
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>{t('common.cancel')}</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>{t('common.grant')}</LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
