import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { defaultLoanAccountSchema, type DefaultLoanAccountFormData } from '../schemas/loanAccountSchema';
import { useDefaultLoanAccount } from '../hooks/useLoanAccounts';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanAccount } from '../api/loanAccountsApi';

interface Props { open: boolean; onClose: () => void; loanAccount: LoanAccount | null; }

const emptyDefaults: DefaultLoanAccountFormData = { reason: '' };

export default function DefaultLoanAccountDialog({ open, onClose, loanAccount }: Props) {
  const { mutateAsync: markDefaulted, isPending } = useDefaultLoanAccount();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<DefaultLoanAccountFormData>({
    resolver: zodResolver(defaultLoanAccountSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: DefaultLoanAccountFormData) => {
    if (!loanAccount?.id) return;
    await markDefaulted({ id: loanAccount.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Mark Loan Account Defaulted</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Reason" fullWidth multiline minRows={2} {...register('reason')} error={!!errors.reason} helperText={errors.reason?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" color="error" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Mark defaulted
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
