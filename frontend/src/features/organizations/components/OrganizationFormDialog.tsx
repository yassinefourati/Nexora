import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useTranslation } from 'react-i18next';
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
  const { t } = useTranslation();
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
      <DialogTitle>{isEdit ? t('resources.organizations.edit') : t('resources.organizations.add')}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label={t('fields.name')} fullWidth {...register('name')} error={!!errors.name} helperText={errors.name?.message} />
          <TextField label={t('fields.code')} fullWidth {...register('code')} error={!!errors.code} helperText={errors.code?.message} />
          <TextField label={t('fields.status')} select fullWidth {...register('status')} error={!!errors.status} helperText={errors.status?.message}>
            {STATUS_OPTIONS.map((s) => <MenuItem key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</MenuItem>)}
          </TextField>
          <TextField label={t('fields.parentOrganization')} select fullWidth {...register('parentOrganizationId')} error={!!errors.parentOrganizationId} helperText={errors.parentOrganizationId?.message}>
            <MenuItem value="">{t('fields.none')}</MenuItem>
            {candidateParents.map((o) => <MenuItem key={o.id} value={o.id}>{o.name}</MenuItem>)}
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>{t('common.cancel')}</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          {isEdit ? t('common.save') : t('common.create')}
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
