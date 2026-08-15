import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack, Alert, Typography } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { createApiKeySchema, type CreateApiKeyFormData } from '../schemas/apiKeySchema';
import { useCreateApiKey } from '../hooks/useApiKeys';
import { useUsers } from '@/features/users/hooks/useUsers';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

export default function ApiKeyFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateApiKey();
  const { data: usersPage } = useUsers({ page: 0, size: 100 });
  const [createdSecret, setCreatedSecret] = useState<string | null>(null);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CreateApiKeyFormData>({
    resolver: zodResolver(createApiKeySchema),
    defaultValues: { name: '', userId: '' },
  });

  const onSubmit = async (data: CreateApiKeyFormData) => {
    const result = await create(data);
    setCreatedSecret(result.secret ?? null);
  };

  const handleClose = () => {
    reset();
    setCreatedSecret(null);
    onClose();
  };

  const users = usersPage?.items ?? [];

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>{createdSecret ? 'API Key Created' : 'Create API Key'}</DialogTitle>
      <DialogContent>
        {createdSecret ? (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Alert severity="warning">This secret is shown only once. Copy it now — it cannot be retrieved again.</Alert>
            <TextField value={createdSecret} fullWidth multiline slotProps={{ input: { readOnly: true, sx: { fontFamily: 'monospace', fontSize: '0.8rem' } } }} />
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Name" fullWidth {...register('name')} error={!!errors.name} helperText={errors.name?.message} />
            <TextField label="User" select fullWidth {...register('userId')} error={!!errors.userId} helperText={errors.userId?.message}>
              {users.map((u) => <MenuItem key={u.id} value={u.id}>{u.username}</MenuItem>)}
            </TextField>
            <Typography variant="caption" color="text.secondary">Leave scopes/expiry unset for a non-expiring, unrestricted key.</Typography>
          </Stack>
        )}
      </DialogContent>
      <DialogActions>
        {createdSecret ? (
          <Button onClick={handleClose} variant="contained">Done</Button>
        ) : (
          <>
            <Button onClick={handleClose} disabled={isPending}>Cancel</Button>
            <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>Create</LoadingButton>
          </>
        )}
      </DialogActions>
    </Dialog>
  );
}
