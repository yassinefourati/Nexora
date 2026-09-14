import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { loanProductSchema, type LoanProductFormData } from '../schemas/loanProductSchema';
import { useCreateLoanProduct, useUpdateLoanProduct } from '../hooks/useLoanProducts';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanProduct } from '../api/loanProductsApi';

interface Props { open: boolean; onClose: () => void; editProduct?: LoanProduct | null; }

const PRODUCT_TYPE_OPTIONS = ['personal', 'consumer', 'auto', 'mortgage', 'business', 'credit_line'];
const STATUS_OPTIONS = ['active', 'inactive', 'retired'];

const emptyDefaults: LoanProductFormData = {
  code: '',
  name: '',
  productType: 'personal',
  status: 'active',
  currency: 'USD',
  minAmount: 0,
  maxAmount: 0,
  minTermMonths: 1,
  maxTermMonths: 1,
  description: '',
};

export default function LoanProductFormDialog({ open, onClose, editProduct }: Props) {
  const isEdit = Boolean(editProduct);
  const { mutateAsync: create, isPending: creating } = useCreateLoanProduct();
  const { mutateAsync: update, isPending: updating } = useUpdateLoanProduct();
  const isPending = creating || updating;

  const { register, handleSubmit, reset, formState: { errors } } = useForm<LoanProductFormData>({
    resolver: zodResolver(loanProductSchema),
    defaultValues: emptyDefaults,
  });

  useEffect(() => {
    reset(editProduct
      ? {
          code: editProduct.code ?? '',
          name: editProduct.name ?? '',
          productType: (editProduct.productType as LoanProductFormData['productType']) ?? 'personal',
          status: editProduct.status ?? 'active',
          currency: editProduct.currency ?? 'USD',
          minAmount: editProduct.minAmount ?? 0,
          maxAmount: editProduct.maxAmount ?? 0,
          minTermMonths: editProduct.minTermMonths ?? 1,
          maxTermMonths: editProduct.maxTermMonths ?? 1,
          description: editProduct.description ?? '',
        }
      : emptyDefaults);
  }, [editProduct, reset]);

  const onSubmit = async (data: LoanProductFormData) => {
    const body = { ...data, description: data.description || undefined };
    if (isEdit && editProduct) {
      await update({ id: editProduct.id!, body });
    } else {
      await create(body);
    }
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Edit Loan Product' : 'Add Loan Product'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Code" fullWidth {...register('code')} error={!!errors.code} helperText={errors.code?.message} />
          <TextField label="Name" fullWidth {...register('name')} error={!!errors.name} helperText={errors.name?.message} />
          <TextField label="Product type" select fullWidth {...register('productType')} error={!!errors.productType} helperText={errors.productType?.message}>
            {PRODUCT_TYPE_OPTIONS.map((t) => <MenuItem key={t} value={t}>{t.replace('_', ' ')}</MenuItem>)}
          </TextField>
          <TextField label="Status" select fullWidth {...register('status')} error={!!errors.status} helperText={errors.status?.message}>
            {STATUS_OPTIONS.map((s) => <MenuItem key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</MenuItem>)}
          </TextField>
          <TextField label="Currency" fullWidth {...register('currency')} error={!!errors.currency} helperText={errors.currency?.message} />
          <TextField label="Min amount" type="number" fullWidth {...register('minAmount', { valueAsNumber: true })} error={!!errors.minAmount} helperText={errors.minAmount?.message} />
          <TextField label="Max amount" type="number" fullWidth {...register('maxAmount', { valueAsNumber: true })} error={!!errors.maxAmount} helperText={errors.maxAmount?.message} />
          <TextField label="Min term (months)" type="number" fullWidth {...register('minTermMonths', { valueAsNumber: true })} error={!!errors.minTermMonths} helperText={errors.minTermMonths?.message} />
          <TextField label="Max term (months)" type="number" fullWidth {...register('maxTermMonths', { valueAsNumber: true })} error={!!errors.maxTermMonths} helperText={errors.maxTermMonths?.message} />
          <TextField label="Description" fullWidth multiline minRows={2} {...register('description')} error={!!errors.description} helperText={errors.description?.message} />
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
