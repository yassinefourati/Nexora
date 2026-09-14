import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { permissionSchema, type PermissionFormData } from '../schemas/permissionSchema';
import { useCreatePermission } from '../hooks/usePermissions';
import { PERMISSION_ACTIONS } from '../api/permissionsApi';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

export default function PermissionFormDialog({ open, onClose }: Props) {
  const { t } = useTranslation();
  const { mutateAsync: create, isPending } = useCreatePermission();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<PermissionFormData>({
    resolver: zodResolver(permissionSchema),
    defaultValues: { resource: '', action: 'read', description: '' },
  });

  const onSubmit = async (data: PermissionFormData) => {
    await create(data);
    reset();
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{t('resources.permissions.add')}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label={t('fields.resource')} fullWidth {...register('resource')} error={!!errors.resource} helperText={errors.resource?.message ?? t('ui.resourceExample')} />
          <TextField label={t('fields.action')} select fullWidth {...register('action')} error={!!errors.action} helperText={errors.action?.message}>
            {PERMISSION_ACTIONS.map((a) => <MenuItem key={a} value={a}>{a}</MenuItem>)}
          </TextField>
          <TextField label={t('fields.description')} fullWidth multiline rows={2} {...register('description')} error={!!errors.description} helperText={errors.description?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>{t('common.cancel')}</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>{t('common.create')}</LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
