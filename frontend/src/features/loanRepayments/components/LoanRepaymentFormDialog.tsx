import { useState } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loanRepaymentSchema, PAYMENT_METHOD_OPTIONS, type LoanRepaymentFormData } from '../schemas/loanRepaymentSchema';
import { useCreateLoanRepayment } from '../hooks/useLoanRepayments';
import { useLoanAccounts, useLoanInstallments } from '@/features/loanAccounts/hooks/useLoanAccounts';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

const emptyDefaults: LoanRepaymentFormData = {
  loanAccountId: '',
  loanInstallmentId: '',
  amount: 0,
  paymentMethod: 'bank_transfer',
};

export default function LoanRepaymentFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateLoanRepayment();
  const { data: loanAccountsPage } = useLoanAccounts({ page: 0, size: 100 });
  const [selectedAccountId, setSelectedAccountId] = useState('');
  const { data: installments } = useLoanInstallments(selectedAccountId || undefined);

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<LoanRepaymentFormData>({
    resolver: zodResolver(loanRepaymentSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: LoanRepaymentFormData) => {
    await create(data);
    reset(emptyDefaults);
    setSelectedAccountId('');
    onClose();
  };

  const activeAccounts = (loanAccountsPage?.items ?? []).filter((a) => a.status === 'active');
  const payableInstallments = (installments ?? []).filter((i) => i.status !== 'paid');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Record Loan Repayment</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            label="Loan account (active)" select fullWidth
            value={selectedAccountId}
            onChange={(e) => { setSelectedAccountId(e.target.value); setValue('loanAccountId', e.target.value); setValue('loanInstallmentId', ''); }}
            error={!!errors.loanAccountId} helperText={errors.loanAccountId?.message}
          >
            {activeAccounts.map((a) => (
              <MenuItem key={a.id} value={a.id}>
                {a.accountNumber} — {a.outstandingPrincipal}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Installment" select fullWidth disabled={!selectedAccountId} {...register('loanInstallmentId')} error={!!errors.loanInstallmentId} helperText={errors.loanInstallmentId?.message}>
            {payableInstallments.map((i) => (
              <MenuItem key={i.id} value={i.id}>
                #{i.installmentNumber} — {i.dueDate} — {i.totalAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Amount" type="number" fullWidth {...register('amount', { valueAsNumber: true })} error={!!errors.amount} helperText={errors.amount?.message} />
          <TextField label="Payment method" select fullWidth {...register('paymentMethod')} error={!!errors.paymentMethod} helperText={errors.paymentMethod?.message}>
            {PAYMENT_METHOD_OPTIONS.map((m) => <MenuItem key={m} value={m}>{m.replace('_', ' ')}</MenuItem>)}
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Record
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
