import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loanOfferSchema, type LoanOfferFormData } from '../schemas/loanOfferSchema';
import { useCreateLoanOffer } from '../hooks/useLoanOffers';
import { useLoanApplications } from '@/features/loanApplications/hooks/useLoanApplications';
import { useLoanApprovals } from '@/features/loanApprovals/hooks/useLoanApprovals';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

const emptyDefaults: LoanOfferFormData = {
  loanApplicationId: '',
  loanApprovalId: '',
  expiresAt: '',
};

export default function LoanOfferFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateLoanOffer();
  const { data: loanApplicationsPage } = useLoanApplications({ page: 0, size: 100 });
  const { data: loanApprovalsPage } = useLoanApprovals({ page: 0, size: 100 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<LoanOfferFormData>({
    resolver: zodResolver(loanOfferSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: LoanOfferFormData) => {
    await create({ ...data, expiresAt: new Date(data.expiresAt).toISOString() });
    reset(emptyDefaults);
    onClose();
  };

  const loanApplications = loanApplicationsPage?.items ?? [];
  const approvedApprovals = (loanApprovalsPage?.items ?? []).filter((a) => a.status === 'approved');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Issue Loan Offer</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Loan application" select fullWidth {...register('loanApplicationId')} error={!!errors.loanApplicationId} helperText={errors.loanApplicationId?.message}>
            {loanApplications.map((a) => (
              <MenuItem key={a.id} value={a.id}>
                {a.purpose ?? a.id} — {a.requestedAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Loan approval (approved)" select fullWidth {...register('loanApprovalId')} error={!!errors.loanApprovalId} helperText={errors.loanApprovalId?.message}>
            {approvedApprovals.map((a) => (
              <MenuItem key={a.id} value={a.id}>
                {a.id} — {a.approvedAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Expires at" type="datetime-local" fullWidth InputLabelProps={{ shrink: true }} {...register('expiresAt')} error={!!errors.expiresAt} helperText={errors.expiresAt?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Issue
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
