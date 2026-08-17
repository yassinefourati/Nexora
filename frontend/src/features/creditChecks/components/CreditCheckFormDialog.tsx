import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { creditCheckSchema, type CreditCheckFormData } from '../schemas/creditCheckSchema';
import { useCreateCreditCheck } from '../hooks/useCreditChecks';
import { useLoanApplications } from '@/features/loanApplications/hooks/useLoanApplications';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

export default function CreditCheckFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateCreditCheck();
  const { data: applicationsPage } = useLoanApplications({ page: 0, size: 100 });

  const { register, handleSubmit, reset, watch, setValue, formState: { errors } } = useForm<CreditCheckFormData>({
    resolver: zodResolver(creditCheckSchema),
    defaultValues: { loanApplicationId: '', customerId: '' },
  });

  const applications = applicationsPage?.items ?? [];
  const loanApplicationId = watch('loanApplicationId');

  const onSubmit = async (data: CreditCheckFormData) => {
    await create(data);
    reset({ loanApplicationId: '', customerId: '' });
    onClose();
  };

  const handleApplicationChange = (id: string) => {
    setValue('loanApplicationId', id);
    const app = applications.find((a) => a.id === id);
    if (app?.customerId) setValue('customerId', app.customerId);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Request Credit Check</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            label="Loan application"
            select
            fullWidth
            value={loanApplicationId}
            onChange={(e) => handleApplicationChange(e.target.value)}
            error={!!errors.loanApplicationId}
            helperText={errors.loanApplicationId?.message}
          >
            {applications.map((a) => (
              <MenuItem key={a.id} value={a.id}>{a.purpose || a.id}</MenuItem>
            ))}
          </TextField>
          <input type="hidden" {...register('customerId')} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Request
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
