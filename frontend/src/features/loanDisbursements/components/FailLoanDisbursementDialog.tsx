import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { failLoanDisbursementSchema, type FailLoanDisbursementFormData } from '../schemas/loanDisbursementSchema';
import { useFailLoanDisbursement } from '../hooks/useLoanDisbursements';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanDisbursement } from '../api/loanDisbursementsApi';

interface Props { open: boolean; onClose: () => void; loanDisbursement: LoanDisbursement | null; }

const emptyDefaults: FailLoanDisbursementFormData = { failureReason: '' };

export default function FailLoanDisbursementDialog({ open, onClose, loanDisbursement }: Props) {
  const { mutateAsync: fail, isPending } = useFailLoanDisbursement();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FailLoanDisbursementFormData>({
    resolver: zodResolver(failLoanDisbursementSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: FailLoanDisbursementFormData) => {
    if (!loanDisbursement?.id) return;
    await fail({ id: loanDisbursement.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Mark Disbursement Failed</DialogTitle>
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
