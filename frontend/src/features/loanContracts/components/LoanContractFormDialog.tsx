import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loanContractSchema, type LoanContractFormData } from '../schemas/loanContractSchema';
import { useCreateLoanContract } from '../hooks/useLoanContracts';
import { useLoanApplications } from '@/features/loanApplications/hooks/useLoanApplications';
import { useLoanOffers } from '@/features/loanOffers/hooks/useLoanOffers';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

const emptyDefaults: LoanContractFormData = {
  loanApplicationId: '',
  loanOfferId: '',
  contractNumber: '',
};

export default function LoanContractFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateLoanContract();
  const { data: loanApplicationsPage } = useLoanApplications({ page: 0, size: 100 });
  const { data: loanOffersPage } = useLoanOffers({ page: 0, size: 100 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<LoanContractFormData>({
    resolver: zodResolver(loanContractSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: LoanContractFormData) => {
    await create(data);
    reset(emptyDefaults);
    onClose();
  };

  const loanApplications = loanApplicationsPage?.items ?? [];
  const acceptedOffers = (loanOffersPage?.items ?? []).filter((o) => o.status === 'accepted');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Generate Loan Contract</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Loan application" select fullWidth {...register('loanApplicationId')} error={!!errors.loanApplicationId} helperText={errors.loanApplicationId?.message}>
            {loanApplications.map((a) => (
              <MenuItem key={a.id} value={a.id}>
                {a.purpose ?? a.id} — {a.requestedAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Loan offer (accepted)" select fullWidth {...register('loanOfferId')} error={!!errors.loanOfferId} helperText={errors.loanOfferId?.message}>
            {acceptedOffers.map((o) => (
              <MenuItem key={o.id} value={o.id}>
                {o.id} — {o.offeredAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Contract number" fullWidth {...register('contractNumber')} error={!!errors.contractNumber} helperText={errors.contractNumber?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Generate
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
