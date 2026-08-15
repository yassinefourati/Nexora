import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack, FormControlLabel, Checkbox } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { roleMenuSchema, type RoleMenuFormData } from '../schemas/roleMenuSchema';
import { useCreateRoleMenu } from '../hooks/useRoleMenus';
import { useRoles } from '@/features/roles/hooks/useRoles';
import { useMenuItems } from '@/features/menus/hooks/useMenus';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

export default function RoleMenuFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateRoleMenu();
  const { data: rolesPage } = useRoles({ page: 0, size: 100 });
  const { data: menuItemsPage } = useMenuItems({ page: 0, size: 200 });

  const { register, control, handleSubmit, reset, formState: { errors } } = useForm<RoleMenuFormData>({
    resolver: zodResolver(roleMenuSchema),
    defaultValues: { roleId: '', menuItemId: '', canView: true },
  });

  const onSubmit = async (data: RoleMenuFormData) => {
    await create(data);
    reset();
    onClose();
  };

  const roles = rolesPage?.items ?? [];
  const menuItems = menuItemsPage?.items ?? [];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Grant Role Menu Access</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Role" select fullWidth {...register('roleId')} error={!!errors.roleId} helperText={errors.roleId?.message}>
            {roles.map((r) => <MenuItem key={r.id} value={r.id}>{r.name}</MenuItem>)}
          </TextField>
          <TextField label="Menu item" select fullWidth {...register('menuItemId')} error={!!errors.menuItemId} helperText={errors.menuItemId?.message}>
            {menuItems.map((mi) => <MenuItem key={mi.id} value={mi.id}>{mi.label}</MenuItem>)}
          </TextField>
          <Controller name="canView" control={control} render={({ field }) => (
            <FormControlLabel control={<Checkbox checked={field.value} onChange={field.onChange} />} label="Can view" />
          )} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>Grant</LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
