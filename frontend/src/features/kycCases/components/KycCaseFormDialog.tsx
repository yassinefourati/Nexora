import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { kycCaseSchema, type KycCaseFormData } from '../schemas/kycCaseSchema';
import { useCreateKycCase } from '../hooks/useKycCases';
import { useCustomers } from '@/features/customers/hooks/useCustomers';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

export default function KycCaseFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateKycCase();
  const { data: customersPage } = useCustomers({ page: 0, size: 100 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<KycCaseFormData>({
    resolver: zodResolver(kycCaseSchema),
    defaultValues: { customerId: '' },
  });

  const onSubmit = async (data: KycCaseFormData) => {
    await create(data);
    reset({ customerId: '' });
    onClose();
  };

  const customers = customersPage?.items ?? [];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Open KYC Case</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Customer" select fullWidth {...register('customerId')} error={!!errors.customerId} helperText={errors.customerId?.message}>
            {customers.map((c) => (
              <MenuItem key={c.id} value={c.id}>
                {c.customerType === 'business' ? c.businessName : `${c.firstName ?? ''} ${c.lastName ?? ''}`.trim()}
              </MenuItem>
            ))}
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Open Case
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
