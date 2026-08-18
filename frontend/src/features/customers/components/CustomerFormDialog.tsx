import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { customerSchema, type CustomerFormData } from '../schemas/customerSchema';
import { useCreateCustomer, useUpdateCustomer } from '../hooks/useCustomers';
import LoadingButton from '@/shared/components/LoadingButton';
import type { Customer } from '../api/customersApi';

interface Props { open: boolean; onClose: () => void; editCustomer?: Customer | null; }

const CUSTOMER_TYPE_OPTIONS = ['individual', 'business'];
const STATUS_OPTIONS = ['active', 'inactive', 'blocked'];

const emptyDefaults: CustomerFormData = {
  customerType: 'individual',
  status: 'active',
  firstName: '',
  lastName: '',
  businessName: '',
  dateOfBirth: '',
  nationalId: '',
  email: '',
  phone: '',
};

export default function CustomerFormDialog({ open, onClose, editCustomer }: Props) {
  const isEdit = Boolean(editCustomer);
  const { mutateAsync: create, isPending: creating } = useCreateCustomer();
  const { mutateAsync: update, isPending: updating } = useUpdateCustomer();
  const isPending = creating || updating;

  const { register, handleSubmit, reset, watch, formState: { errors } } = useForm<CustomerFormData>({
    resolver: zodResolver(customerSchema),
    defaultValues: emptyDefaults,
  });

  const customerType = watch('customerType');

  useEffect(() => {
    reset(editCustomer
      ? {
          customerType: (editCustomer.customerType as 'individual' | 'business') ?? 'individual',
          status: editCustomer.status ?? 'active',
          firstName: editCustomer.firstName ?? '',
          lastName: editCustomer.lastName ?? '',
          businessName: editCustomer.businessName ?? '',
          dateOfBirth: editCustomer.dateOfBirth ?? '',
          nationalId: editCustomer.nationalId ?? '',
          email: editCustomer.email ?? '',
          phone: editCustomer.phone ?? '',
        }
      : emptyDefaults);
  }, [editCustomer, reset]);

  const onSubmit = async (data: CustomerFormData) => {
    const body = {
      ...data,
      firstName: data.firstName || undefined,
      lastName: data.lastName || undefined,
      businessName: data.businessName || undefined,
      dateOfBirth: data.dateOfBirth || undefined,
      nationalId: data.nationalId || undefined,
      phone: data.phone || undefined,
    };
    if (isEdit && editCustomer) {
      await update({ id: editCustomer.id!, body });
    } else {
      await create(body);
    }
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Edit Customer' : 'Add Customer'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Customer type" select fullWidth {...register('customerType')} error={!!errors.customerType} helperText={errors.customerType?.message}>
            {CUSTOMER_TYPE_OPTIONS.map((t) => <MenuItem key={t} value={t}>{t.charAt(0).toUpperCase() + t.slice(1)}</MenuItem>)}
          </TextField>
          <TextField label="Status" select fullWidth {...register('status')} error={!!errors.status} helperText={errors.status?.message}>
            {STATUS_OPTIONS.map((s) => <MenuItem key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</MenuItem>)}
          </TextField>
          {customerType === 'business' ? (
            <TextField label="Business name" fullWidth {...register('businessName')} error={!!errors.businessName} helperText={errors.businessName?.message} />
          ) : (
            <>
              <TextField label="First name" fullWidth {...register('firstName')} error={!!errors.firstName} helperText={errors.firstName?.message} />
              <TextField label="Last name" fullWidth {...register('lastName')} error={!!errors.lastName} helperText={errors.lastName?.message} />
              <TextField label="Date of birth" type="date" fullWidth InputLabelProps={{ shrink: true }} {...register('dateOfBirth')} error={!!errors.dateOfBirth} helperText={errors.dateOfBirth?.message} />
            </>
          )}
          <TextField label="National ID / Tax ID" fullWidth {...register('nationalId')} error={!!errors.nationalId} helperText={errors.nationalId?.message} />
          <TextField label="Email" fullWidth {...register('email')} error={!!errors.email} helperText={errors.email?.message} />
          <TextField label="Phone" fullWidth {...register('phone')} error={!!errors.phone} helperText={errors.phone?.message} />
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
