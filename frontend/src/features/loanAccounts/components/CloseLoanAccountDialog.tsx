import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { closeLoanAccountSchema, type CloseLoanAccountFormData } from '../schemas/loanAccountSchema';
import { useCloseLoanAccount } from '../hooks/useLoanAccounts';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanAccount } from '../api/loanAccountsApi';

interface Props { open: boolean; onClose: () => void; loanAccount: LoanAccount | null; }

const emptyDefaults: CloseLoanAccountFormData = { reason: '' };

export default function CloseLoanAccountDialog({ open, onClose, loanAccount }: Props) {
  const { mutateAsync: close, isPending } = useCloseLoanAccount();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CloseLoanAccountFormData>({
    resolver: zodResolver(closeLoanAccountSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: CloseLoanAccountFormData) => {
    if (!loanAccount?.id) return;
    await close({ id: loanAccount.id, body: { reason: data.reason || undefined } });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Close Loan Account</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Reason" fullWidth multiline minRows={2} {...register('reason')} error={!!errors.reason} helperText={errors.reason?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Close account
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
