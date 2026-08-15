import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { organizationSchema, type OrganizationFormData } from '../schemas/organizationSchema';
import { useCreateOrganization, useUpdateOrganization, useOrganizations } from '../hooks/useOrganizations';
import LoadingButton from '@/shared/components/LoadingButton';
import type { Organization } from '../api/organizationsApi';

interface Props { open: boolean; onClose: () => void; editOrg?: Organization | null; }

const STATUS_OPTIONS = ['active', 'inactive', 'suspended'];

export default function OrganizationFormDialog({ open, onClose, editOrg }: Props) {
  const isEdit = Boolean(editOrg);
  const { mutateAsync: create, isPending: creating } = useCreateOrganization();
  const { mutateAsync: update, isPending: updating } = useUpdateOrganization();
  const { data: orgsPage } = useOrganizations({ page: 0, size: 100 });
  const isPending = creating || updating;

  const { register, handleSubmit, reset, formState: { errors } } = useForm<OrganizationFormData>({
    resolver: zodResolver(organizationSchema),
    defaultValues: { name: '', code: '', status: 'active', parentOrganizationId: '' },
  });

  useEffect(() => {
    reset(editOrg
      ? { name: editOrg.name ?? '', code: editOrg.code ?? '', status: editOrg.status ?? 'active', parentOrganizationId: editOrg.parentOrganizationId ?? '' }
      : { name: '', code: '', status: 'active', parentOrganizationId: '' });
  }, [editOrg, reset]);

  const onSubmit = async (data: OrganizationFormData) => {
    const body = { ...data, parentOrganizationId: data.parentOrganizationId || undefined };
    if (isEdit && editOrg) {
      await update({ id: editOrg.id!, body });
    } else {
      await create(body);
    }
    onClose();
  };

  const candidateParents = (orgsPage?.items ?? []).filter((o) => o.id !== editOrg?.id);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Edit Organization' : 'Add Organization'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Name" fullWidth {...register('name')} error={!!errors.name} helperText={errors.name?.message} />
          <TextField label="Code" fullWidth {...register('code')} error={!!errors.code} helperText={errors.code?.message} />
          <TextField label="Status" select fullWidth {...register('status')} error={!!errors.status} helperText={errors.status?.message}>
            {STATUS_OPTIONS.map((s) => <MenuItem key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</MenuItem>)}
          </TextField>
          <TextField label="Parent organization" select fullWidth {...register('parentOrganizationId')} error={!!errors.parentOrganizationId} helperText={errors.parentOrganizationId?.message}>
            <MenuItem value="">None</MenuItem>
            {candidateParents.map((o) => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
          </TextField>
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
