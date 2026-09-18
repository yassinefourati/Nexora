import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { completeLoanRepaymentSchema, type CompleteLoanRepaymentFormData } from '../schemas/loanRepaymentSchema';
import { useCompleteLoanRepayment } from '../hooks/useLoanRepayments';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanRepayment } from '../api/loanRepaymentsApi';

interface Props { open: boolean; onClose: () => void; loanRepayment: LoanRepayment | null; }

const emptyDefaults: CompleteLoanRepaymentFormData = { referenceNumber: '' };

export default function CompleteLoanRepaymentDialog({ open, onClose, loanRepayment }: Props) {
  const { mutateAsync: complete, isPending } = useCompleteLoanRepayment();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CompleteLoanRepaymentFormData>({
    resolver: zodResolver(completeLoanRepaymentSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: CompleteLoanRepaymentFormData) => {
    if (!loanRepayment?.id) return;
    await complete({ id: loanRepayment.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Complete Loan Repayment</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Reference number" fullWidth {...register('referenceNumber')} error={!!errors.referenceNumber} helperText={errors.referenceNumber?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Complete
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
