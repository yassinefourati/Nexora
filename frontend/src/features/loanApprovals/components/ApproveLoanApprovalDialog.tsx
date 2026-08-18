import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { approveLoanApprovalSchema, type ApproveLoanApprovalFormData } from '../schemas/loanApprovalSchema';
import { useApproveLoanApproval } from '../hooks/useLoanApprovals';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanApproval } from '../api/loanApprovalsApi';

interface Props { open: boolean; onClose: () => void; loanApproval: LoanApproval | null; }

const emptyDefaults: ApproveLoanApprovalFormData = {
  approvedAmount: 0,
  approvedTermMonths: 1,
  interestRate: 0,
  approvedBy: '',
};

export default function ApproveLoanApprovalDialog({ open, onClose, loanApproval }: Props) {
  const { mutateAsync: approve, isPending } = useApproveLoanApproval();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<ApproveLoanApprovalFormData>({
    resolver: zodResolver(approveLoanApprovalSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: ApproveLoanApprovalFormData) => {
    if (!loanApproval?.id) return;
    await approve({ id: loanApproval.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Approve Loan</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Approved amount" type="number" fullWidth {...register('approvedAmount', { valueAsNumber: true })} error={!!errors.approvedAmount} helperText={errors.approvedAmount?.message} />
          <TextField label="Approved term (months)" type="number" fullWidth {...register('approvedTermMonths', { valueAsNumber: true })} error={!!errors.approvedTermMonths} helperText={errors.approvedTermMonths?.message} />
          <TextField label="Interest rate (%)" type="number" fullWidth {...register('interestRate', { valueAsNumber: true })} error={!!errors.interestRate} helperText={errors.interestRate?.message} />
          <TextField label="Approved by" fullWidth {...register('approvedBy')} error={!!errors.approvedBy} helperText={errors.approvedBy?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Approve
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
