import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { fraudCheckSchema, type FraudCheckFormData } from '../schemas/fraudCheckSchema';
import { useCreateFraudCheck } from '../hooks/useFraudChecks';
import { useLoanApplications } from '@/features/loanApplications/hooks/useLoanApplications';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

export default function FraudCheckFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateFraudCheck();
  const { data: applicationsPage } = useLoanApplications({ page: 0, size: 100 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FraudCheckFormData>({
    resolver: zodResolver(fraudCheckSchema),
    defaultValues: { loanApplicationId: '' },
  });

  const applications = applicationsPage?.items ?? [];

  const onSubmit = async (data: FraudCheckFormData) => {
    await create(data);
    reset({ loanApplicationId: '' });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Open Fraud Check</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Loan application" select fullWidth {...register('loanApplicationId')} error={!!errors.loanApplicationId} helperText={errors.loanApplicationId?.message}>
            {applications.map((a) => (
              <MenuItem key={a.id} value={a.id}>{a.purpose || a.id}</MenuItem>
            ))}
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Open Check
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
