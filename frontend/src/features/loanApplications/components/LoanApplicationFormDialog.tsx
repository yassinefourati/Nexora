import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { loanApplicationSchema, type LoanApplicationFormData } from '../schemas/loanApplicationSchema';
import { useCreateLoanApplication, useUpdateLoanApplication } from '../hooks/useLoanApplications';
import { useCustomers } from '@/features/customers/hooks/useCustomers';
import { useLoanProducts } from '@/features/loanProducts/hooks/useLoanProducts';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanApplication } from '../api/loanApplicationsApi';

interface Props { open: boolean; onClose: () => void; editApplication?: LoanApplication | null; }

const emptyDefaults: LoanApplicationFormData = {
  customerId: '',
  loanProductId: '',
  requestedAmount: 0,
  requestedTermMonths: 1,
  purpose: '',
};

export default function LoanApplicationFormDialog({ open, onClose, editApplication }: Props) {
  const isEdit = Boolean(editApplication);
  const { mutateAsync: create, isPending: creating } = useCreateLoanApplication();
  const { mutateAsync: update, isPending: updating } = useUpdateLoanApplication();
  const { data: customersPage } = useCustomers({ page: 0, size: 100 });
  const { data: loanProductsPage } = useLoanProducts({ page: 0, size: 100 });
  const isPending = creating || updating;

  const { register, handleSubmit, reset, formState: { errors } } = useForm<LoanApplicationFormData>({
    resolver: zodResolver(loanApplicationSchema),
    defaultValues: emptyDefaults,
  });

  useEffect(() => {
    reset(editApplication
      ? {
          customerId: editApplication.customerId ?? '',
          loanProductId: editApplication.loanProductId ?? '',
          requestedAmount: editApplication.requestedAmount ?? 0,
          requestedTermMonths: editApplication.requestedTermMonths ?? 1,
          purpose: editApplication.purpose ?? '',
        }
      : emptyDefaults);
  }, [editApplication, reset]);

  const onSubmit = async (data: LoanApplicationFormData) => {
    const body = { ...data, purpose: data.purpose || undefined };
    if (isEdit && editApplication) {
      await update({ id: editApplication.id!, body });
    } else {
      await create(body);
    }
    onClose();
  };

  const customers = customersPage?.items ?? [];
  const loanProducts = loanProductsPage?.items ?? [];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Edit Loan Application' : 'Add Loan Application'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Customer" select fullWidth disabled={isEdit} {...register('customerId')} error={!!errors.customerId} helperText={errors.customerId?.message}>
            {customers.map((c) => (
              <MenuItem key={c.id} value={c.id}>
                {c.customerType === 'business' ? c.businessName : `${c.firstName ?? ''} ${c.lastName ?? ''}`.trim()}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Loan product" select fullWidth disabled={isEdit} {...register('loanProductId')} error={!!errors.loanProductId} helperText={errors.loanProductId?.message}>
            {loanProducts.map((p) => <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>)}
          </TextField>
          <TextField label="Requested amount" type="number" fullWidth {...register('requestedAmount', { valueAsNumber: true })} error={!!errors.requestedAmount} helperText={errors.requestedAmount?.message} />
          <TextField label="Requested term (months)" type="number" fullWidth {...register('requestedTermMonths', { valueAsNumber: true })} error={!!errors.requestedTermMonths} helperText={errors.requestedTermMonths?.message} />
          <TextField label="Purpose" fullWidth {...register('purpose')} error={!!errors.purpose} helperText={errors.purpose?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          {isEdit ? 'Save' : 'Create'}
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
