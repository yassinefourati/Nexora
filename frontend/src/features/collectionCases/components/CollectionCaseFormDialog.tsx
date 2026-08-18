import { useState } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { collectionCaseSchema, type CollectionCaseFormData } from '../schemas/collectionCaseSchema';
import { useCreateCollectionCase } from '../hooks/useCollectionCases';
import { useLoanAccounts, useLoanInstallments } from '@/features/loanAccounts/hooks/useLoanAccounts';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

const emptyDefaults: CollectionCaseFormData = {
  loanAccountId: '',
  loanInstallmentId: '',
  assignedTo: '',
};

const isOverdue = (dueDate: string | undefined) => !!dueDate && new Date(dueDate) < new Date();

export default function CollectionCaseFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateCollectionCase();
  const { data: loanAccountsPage } = useLoanAccounts({ page: 0, size: 100 });
  const [selectedAccountId, setSelectedAccountId] = useState('');
  const { data: installments } = useLoanInstallments(selectedAccountId || undefined);

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<CollectionCaseFormData>({
    resolver: zodResolver(collectionCaseSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: CollectionCaseFormData) => {
    await create({ ...data, assignedTo: data.assignedTo || undefined });
    reset(emptyDefaults);
    setSelectedAccountId('');
    onClose();
  };

  const activeAccounts = (loanAccountsPage?.items ?? []).filter((a) => a.status === 'active');
  const overdueInstallments = (installments ?? []).filter((i) => i.status !== 'paid' && isOverdue(i.dueDate));

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Open Collection Case</DialogTitle>
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
          <TextField label="Overdue installment" select fullWidth disabled={!selectedAccountId} {...register('loanInstallmentId')} error={!!errors.loanInstallmentId} helperText={errors.loanInstallmentId?.message}>
            {overdueInstallments.map((i) => (
              <MenuItem key={i.id} value={i.id}>
                #{i.installmentNumber} — due {i.dueDate} — {i.totalAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Assigned to" fullWidth {...register('assignedTo')} error={!!errors.assignedTo} helperText={errors.assignedTo?.message} />
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
