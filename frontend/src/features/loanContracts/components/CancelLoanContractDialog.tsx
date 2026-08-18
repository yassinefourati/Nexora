import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { cancelLoanContractSchema, type CancelLoanContractFormData } from '../schemas/loanContractSchema';
import { useCancelLoanContract } from '../hooks/useLoanContracts';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanContract } from '../api/loanContractsApi';

interface Props { open: boolean; onClose: () => void; loanContract: LoanContract | null; }

const emptyDefaults: CancelLoanContractFormData = { cancellationReason: '' };

export default function CancelLoanContractDialog({ open, onClose, loanContract }: Props) {
  const { mutateAsync: cancel, isPending } = useCancelLoanContract();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CancelLoanContractFormData>({
    resolver: zodResolver(cancelLoanContractSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: CancelLoanContractFormData) => {
    if (!loanContract?.id) return;
    await cancel({ id: loanContract.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Cancel Loan Contract</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Cancellation reason" fullWidth multiline minRows={2} {...register('cancellationReason')} error={!!errors.cancellationReason} helperText={errors.cancellationReason?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" color="error" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Cancel contract
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
