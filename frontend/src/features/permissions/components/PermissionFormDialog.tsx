import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { permissionSchema, type PermissionFormData } from '../schemas/permissionSchema';
import { useCreatePermission } from '../hooks/usePermissions';
import { PERMISSION_ACTIONS } from '../api/permissionsApi';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

export default function PermissionFormDialog({ open, onClose }: Props) {
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
      <DialogTitle>Add Permission</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Resource" fullWidth {...register('resource')} error={!!errors.resource} helperText={errors.resource?.message ?? 'e.g. users, roles, organizations'} />
          <TextField label="Action" select fullWidth {...register('action')} error={!!errors.action} helperText={errors.action?.message}>
            {PERMISSION_ACTIONS.map((a) => <MenuItem key={a} value={a}>{a}</MenuItem>)}
          </TextField>
          <TextField label="Description" fullWidth multiline rows={2} {...register('description')} error={!!errors.description} helperText={errors.description?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>Create</LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
