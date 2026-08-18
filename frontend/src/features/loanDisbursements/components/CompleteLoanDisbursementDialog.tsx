import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { completeLoanDisbursementSchema, type CompleteLoanDisbursementFormData } from '../schemas/loanDisbursementSchema';
import { useCompleteLoanDisbursement } from '../hooks/useLoanDisbursements';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanDisbursement } from '../api/loanDisbursementsApi';

interface Props { open: boolean; onClose: () => void; loanDisbursement: LoanDisbursement | null; }

const emptyDefaults: CompleteLoanDisbursementFormData = { referenceNumber: '' };

export default function CompleteLoanDisbursementDialog({ open, onClose, loanDisbursement }: Props) {
  const { mutateAsync: complete, isPending } = useCompleteLoanDisbursement();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CompleteLoanDisbursementFormData>({
    resolver: zodResolver(completeLoanDisbursementSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: CompleteLoanDisbursementFormData) => {
    if (!loanDisbursement?.id) return;
    await complete({ id: loanDisbursement.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Complete Loan Disbursement</DialogTitle>
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
