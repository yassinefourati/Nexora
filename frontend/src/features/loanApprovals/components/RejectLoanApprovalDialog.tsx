import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { rejectLoanApprovalSchema, type RejectLoanApprovalFormData } from '../schemas/loanApprovalSchema';
import { useRejectLoanApproval } from '../hooks/useLoanApprovals';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanApproval } from '../api/loanApprovalsApi';

interface Props { open: boolean; onClose: () => void; loanApproval: LoanApproval | null; }

const emptyDefaults: RejectLoanApprovalFormData = { rejectionReason: '' };

export default function RejectLoanApprovalDialog({ open, onClose, loanApproval }: Props) {
  const { mutateAsync: reject, isPending } = useRejectLoanApproval();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<RejectLoanApprovalFormData>({
    resolver: zodResolver(rejectLoanApprovalSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: RejectLoanApprovalFormData) => {
    if (!loanApproval?.id) return;
    await reject({ id: loanApproval.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Reject Loan Approval</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Rejection reason" fullWidth multiline minRows={2} {...register('rejectionReason')} error={!!errors.rejectionReason} helperText={errors.rejectionReason?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" color="error" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Reject
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
