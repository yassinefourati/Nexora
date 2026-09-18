import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { finalizeLoanContractSchema, type FinalizeLoanContractFormData } from '../schemas/loanContractSchema';
import { useFinalizeLoanContract } from '../hooks/useLoanContracts';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanContract } from '../api/loanContractsApi';

interface Props { open: boolean; onClose: () => void; loanContract: LoanContract | null; }

const emptyDefaults: FinalizeLoanContractFormData = { documentUrl: '' };

export default function FinalizeLoanContractDialog({ open, onClose, loanContract }: Props) {
  const { mutateAsync: finalizeContract, isPending } = useFinalizeLoanContract();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FinalizeLoanContractFormData>({
    resolver: zodResolver(finalizeLoanContractSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: FinalizeLoanContractFormData) => {
    if (!loanContract?.id) return;
    await finalizeContract({ id: loanContract.id, body: { documentUrl: data.documentUrl || undefined } });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Finalize Loan Contract</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Document URL" fullWidth {...register('documentUrl')} error={!!errors.documentUrl} helperText={errors.documentUrl?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Finalize
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
