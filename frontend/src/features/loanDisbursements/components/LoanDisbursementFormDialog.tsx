import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loanDisbursementSchema, DISBURSEMENT_METHOD_OPTIONS, type LoanDisbursementFormData } from '../schemas/loanDisbursementSchema';
import { useCreateLoanDisbursement } from '../hooks/useLoanDisbursements';
import { useLoanApplications } from '@/features/loanApplications/hooks/useLoanApplications';
import { useLoanContracts } from '@/features/loanContracts/hooks/useLoanContracts';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

const emptyDefaults: LoanDisbursementFormData = {
  loanApplicationId: '',
  loanContractId: '',
  disbursementMethod: 'bank_transfer',
  destinationAccount: '',
};

export default function LoanDisbursementFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateLoanDisbursement();
  const { data: loanApplicationsPage } = useLoanApplications({ page: 0, size: 100 });
  const { data: loanContractsPage } = useLoanContracts({ page: 0, size: 100 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<LoanDisbursementFormData>({
    resolver: zodResolver(loanDisbursementSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: LoanDisbursementFormData) => {
    await create(data);
    reset(emptyDefaults);
    onClose();
  };

  const loanApplications = loanApplicationsPage?.items ?? [];
  const finalizedContracts = (loanContractsPage?.items ?? []).filter((c) => c.status === 'finalized');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Create Loan Disbursement</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Loan application" select fullWidth {...register('loanApplicationId')} error={!!errors.loanApplicationId} helperText={errors.loanApplicationId?.message}>
            {loanApplications.map((a) => (
              <MenuItem key={a.id} value={a.id}>
                {a.purpose ?? a.id} — {a.requestedAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Loan contract (finalized)" select fullWidth {...register('loanContractId')} error={!!errors.loanContractId} helperText={errors.loanContractId?.message}>
            {finalizedContracts.map((c) => (
              <MenuItem key={c.id} value={c.id}>
                {c.contractNumber} — {c.principalAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Disbursement method" select fullWidth {...register('disbursementMethod')} error={!!errors.disbursementMethod} helperText={errors.disbursementMethod?.message}>
            {DISBURSEMENT_METHOD_OPTIONS.map((m) => <MenuItem key={m} value={m}>{m.replace('_', ' ')}</MenuItem>)}
          </TextField>
          <TextField label="Destination account" fullWidth {...register('destinationAccount')} error={!!errors.destinationAccount} helperText={errors.destinationAccount?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Create
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
