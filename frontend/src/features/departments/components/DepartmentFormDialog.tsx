import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { createDepartmentSchema, updateDepartmentSchema, type CreateDepartmentFormData, type UpdateDepartmentFormData } from '../schemas/departmentSchema';
import { useCreateDepartment, useUpdateDepartment, useDepartments } from '../hooks/useDepartments';
import { useOrganizations } from '@/features/organizations/hooks/useOrganizations';
import LoadingButton from '@/shared/components/LoadingButton';
import type { Department } from '../api/departmentsApi';

interface Props { open: boolean; onClose: () => void; editDept?: Department | null; }

export default function DepartmentFormDialog({ open, onClose, editDept }: Props) {
  const isEdit = Boolean(editDept);
  const { mutateAsync: create, isPending: creating } = useCreateDepartment();
  const { mutateAsync: update, isPending: updating } = useUpdateDepartment();
  const { data: orgsPage } = useOrganizations({ page: 0, size: 100 });
  const { data: deptsPage } = useDepartments({ page: 0, size: 100 });
  const isPending = creating || updating;

  const createForm = useForm<CreateDepartmentFormData>({
    resolver: zodResolver(createDepartmentSchema),
    defaultValues: { name: '', code: '', organizationId: '', parentDepartmentId: '' },
  });
  const updateForm = useForm<UpdateDepartmentFormData>({
    resolver: zodResolver(updateDepartmentSchema),
    defaultValues: { name: '', code: '', parentDepartmentId: '' },
  });

  useEffect(() => {
    if (editDept) {
      updateForm.reset({ name: editDept.name ?? '', code: editDept.code ?? '', parentDepartmentId: editDept.parentDepartmentId ?? '' });
    } else {
      createForm.reset({ name: '', code: '', organizationId: '', parentDepartmentId: '' });
    }
  }, [editDept, createForm, updateForm]);

  const onSubmitCreate = async (data: CreateDepartmentFormData) => {
    await create({ ...data, parentDepartmentId: data.parentDepartmentId || undefined });
    onClose();
  };
  const onSubmitUpdate = async (data: UpdateDepartmentFormData) => {
    if (!editDept) return;
    await update({ id: editDept.id!, body: { ...data, parentDepartmentId: data.parentDepartmentId || undefined } });
    onClose();
  };

  const orgs = orgsPage?.items ?? [];
  const candidateParents = (deptsPage?.items ?? []).filter((d) => d.id !== editDept?.id);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Edit Department' : 'Add Department'}</DialogTitle>
      <DialogContent>
        {isEdit ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Name" fullWidth {...updateForm.register('name')} error={!!updateForm.formState.errors.name} helperText={updateForm.formState.errors.name?.message} />
            <TextField label="Code" fullWidth {...updateForm.register('code')} error={!!updateForm.formState.errors.code} helperText={updateForm.formState.errors.code?.message} />
            <TextField label="Parent department" select fullWidth {...updateForm.register('parentDepartmentId')}>
              <MenuItem value="">None</MenuItem>
              {candidateParents.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
            </TextField>
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Name" fullWidth {...createForm.register('name')} error={!!createForm.formState.errors.name} helperText={createForm.formState.errors.name?.message} />
            <TextField label="Code" fullWidth {...createForm.register('code')} error={!!createForm.formState.errors.code} helperText={createForm.formState.errors.code?.message} />
            <TextField label="Organization" select fullWidth {...createForm.register('organizationId')} error={!!createForm.formState.errors.organizationId} helperText={createForm.formState.errors.organizationId?.message}>
              {orgs.map((o) => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
            </TextField>
            <TextField label="Parent department" select fullWidth {...createForm.register('parentDepartmentId')}>
              <MenuItem value="">None</MenuItem>
              {candidateParents.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
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
