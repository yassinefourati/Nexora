import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loanAccountSchema, type LoanAccountFormData } from '../schemas/loanAccountSchema';
import { useCreateLoanAccount } from '../hooks/useLoanAccounts';
import { useLoanApplications } from '@/features/loanApplications/hooks/useLoanApplications';
import { useLoanDisbursements } from '@/features/loanDisbursements/hooks/useLoanDisbursements';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

const emptyDefaults: LoanAccountFormData = {
  loanApplicationId: '',
  loanDisbursementId: '',
  accountNumber: '',
};

export default function LoanAccountFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateLoanAccount();
  const { data: loanApplicationsPage } = useLoanApplications({ page: 0, size: 100 });
  const { data: loanDisbursementsPage } = useLoanDisbursements({ page: 0, size: 100 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<LoanAccountFormData>({
    resolver: zodResolver(loanAccountSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: LoanAccountFormData) => {
    await create(data);
    reset(emptyDefaults);
    onClose();
  };

  const loanApplications = loanApplicationsPage?.items ?? [];
  const completedDisbursements = (loanDisbursementsPage?.items ?? []).filter((d) => d.status === 'completed');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Open Loan Account</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Loan application" select fullWidth {...register('loanApplicationId')} error={!!errors.loanApplicationId} helperText={errors.loanApplicationId?.message}>
            {loanApplications.map((a) => (
              <MenuItem key={a.id} value={a.id}>
                {a.purpose ?? a.id} — {a.requestedAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Loan disbursement (completed)" select fullWidth {...register('loanDisbursementId')} error={!!errors.loanDisbursementId} helperText={errors.loanDisbursementId?.message}>
            {completedDisbursements.map((d) => (
              <MenuItem key={d.id} value={d.id}>
                {d.id} — {d.amount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Account number" fullWidth {...register('accountNumber')} error={!!errors.accountNumber} helperText={errors.accountNumber?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Open
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
