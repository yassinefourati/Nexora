import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { failLoanRepaymentSchema, type FailLoanRepaymentFormData } from '../schemas/loanRepaymentSchema';
import { useFailLoanRepayment } from '../hooks/useLoanRepayments';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanRepayment } from '../api/loanRepaymentsApi';

interface Props { open: boolean; onClose: () => void; loanRepayment: LoanRepayment | null; }

const emptyDefaults: FailLoanRepaymentFormData = { failureReason: '' };

export default function FailLoanRepaymentDialog({ open, onClose, loanRepayment }: Props) {
  const { mutateAsync: fail, isPending } = useFailLoanRepayment();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FailLoanRepaymentFormData>({
    resolver: zodResolver(failLoanRepaymentSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: FailLoanRepaymentFormData) => {
    if (!loanRepayment?.id) return;
    await fail({ id: loanRepayment.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Mark Repayment Failed</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Failure reason" fullWidth multiline minRows={2} {...register('failureReason')} error={!!errors.failureReason} helperText={errors.failureReason?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" color="error" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Mark failed
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
