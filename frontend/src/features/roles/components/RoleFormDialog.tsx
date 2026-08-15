import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack, FormControlLabel, Checkbox } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { roleSchema, type RoleFormData } from '../schemas/roleSchema';
import { useCreateRole, useUpdateRole } from '../hooks/useRoles';
import LoadingButton from '@/shared/components/LoadingButton';
import type { Role } from '../api/rolesApi';

interface Props { open: boolean; onClose: () => void; editRole?: Role | null; }

export default function RoleFormDialog({ open, onClose, editRole }: Props) {
  const isEdit = Boolean(editRole);
  const { mutateAsync: create, isPending: creating } = useCreateRole();
  const { mutateAsync: update, isPending: updating } = useUpdateRole();
  const isPending = creating || updating;

  const { register, handleSubmit, reset, formState: { errors } } = useForm<RoleFormData>({
    resolver: zodResolver(roleSchema),
    defaultValues: { name: '', description: '', system: false },
  });

  useEffect(() => {
    reset(editRole
      ? { name: editRole.name ?? '', description: editRole.description ?? '', system: editRole.system ?? false }
      : { name: '', description: '', system: false });
  }, [editRole, reset]);

  const onSubmit = async (data: RoleFormData) => {
    if (isEdit && editRole) {
      await update({ id: editRole.id!, body: data });
    } else {
      await create(data);
    }
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Edit Role' : 'Add Role'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Name" fullWidth {...register('name')} error={!!errors.name} helperText={errors.name?.message} />
          <TextField label="Description" fullWidth multiline rows={2} {...register('description')} error={!!errors.description} helperText={errors.description?.message} />
          <FormControlLabel control={<Checkbox {...register('system')} />} label="System role (protected)" />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          {isEdit ? 'Save' : 'Create'}
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
