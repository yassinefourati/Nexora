import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { createTeamSchema, updateTeamSchema, type CreateTeamFormData, type UpdateTeamFormData } from '../schemas/teamSchema';
import { useCreateTeam, useUpdateTeam } from '../hooks/useTeams';
import { useOrganizations } from '@/features/organizations/hooks/useOrganizations';
import { useDepartments } from '@/features/departments/hooks/useDepartments';
import LoadingButton from '@/shared/components/LoadingButton';
import type { Team } from '../api/teamsApi';

interface Props { open: boolean; onClose: () => void; editTeam?: Team | null; }

export default function TeamFormDialog({ open, onClose, editTeam }: Props) {
  const isEdit = Boolean(editTeam);
  const { mutateAsync: create, isPending: creating } = useCreateTeam();
  const { mutateAsync: update, isPending: updating } = useUpdateTeam();
  const { data: orgsPage } = useOrganizations({ page: 0, size: 100 });
  const { data: deptsPage } = useDepartments({ page: 0, size: 100 });
  const isPending = creating || updating;

  const createForm = useForm<CreateTeamFormData>({
    resolver: zodResolver(createTeamSchema),
    defaultValues: { name: '', organizationId: '', departmentId: '' },
  });
  const updateForm = useForm<UpdateTeamFormData>({
    resolver: zodResolver(updateTeamSchema),
    defaultValues: { name: '', departmentId: '' },
  });

  useEffect(() => {
    if (editTeam) {
      updateForm.reset({ name: editTeam.name ?? '', departmentId: editTeam.departmentId ?? '' });
    } else {
      createForm.reset({ name: '', organizationId: '', departmentId: '' });
    }
  }, [editTeam, createForm, updateForm]);

  const onSubmitCreate = async (data: CreateTeamFormData) => {
    await create({ ...data, departmentId: data.departmentId || undefined });
    onClose();
  };
  const onSubmitUpdate = async (data: UpdateTeamFormData) => {
    if (!editTeam) return;
    await update({ id: editTeam.id!, body: { ...data, departmentId: data.departmentId || undefined } });
    onClose();
  };

  const orgs = orgsPage?.items ?? [];
  const depts = deptsPage?.items ?? [];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Edit Team' : 'Add Team'}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Name" fullWidth {...updateForm.register('name')} error={!!updateForm.formState.errors.name} helperText={updateForm.formState.errors.name?.message} />
            <TextField label="Department" select fullWidth {...updateForm.register('departmentId')}>
              <MenuItem value="">None</MenuItem>
              {depts.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
            </TextField>
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Name" fullWidth {...createForm.register('name')} error={!!createForm.formState.errors.name} helperText={createForm.formState.errors.name?.message} />
            <TextField label="Organization" select fullWidth {...createForm.register('organizationId')} error={!!createForm.formState.errors.organizationId} helperText={createForm.formState.errors.organizationId?.message}>
              {orgs.map((o) => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
            </TextField>
            <TextField label="Department" select fullWidth {...createForm.register('departmentId')}>
              <MenuItem value="">None</MenuItem>
              {depts.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
            </TextField>
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
