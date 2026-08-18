import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loanApprovalSchema, type LoanApprovalFormData } from '../schemas/loanApprovalSchema';
import { useCreateLoanApproval } from '../hooks/useLoanApprovals';
import { useLoanApplications } from '@/features/loanApplications/hooks/useLoanApplications';
import { useUnderwritingCases } from '@/features/underwritingCases/hooks/useUnderwritingCases';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

const emptyDefaults: LoanApprovalFormData = {
  loanApplicationId: '',
  underwritingCaseId: '',
};

export default function LoanApprovalFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateLoanApproval();
  const { data: loanApplicationsPage } = useLoanApplications({ page: 0, size: 100 });
  const { data: underwritingCasesPage } = useUnderwritingCases({ page: 0, size: 100 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<LoanApprovalFormData>({
    resolver: zodResolver(loanApprovalSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: LoanApprovalFormData) => {
    await create(data);
    reset(emptyDefaults);
    onClose();
  };

  const loanApplications = loanApplicationsPage?.items ?? [];
  const completedCases = (underwritingCasesPage?.items ?? []).filter((c) => c.status === 'completed');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Open Loan Approval</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Loan application" select fullWidth {...register('loanApplicationId')} error={!!errors.loanApplicationId} helperText={errors.loanApplicationId?.message}>
            {loanApplications.map((a) => (
              <MenuItem key={a.id} value={a.id}>
                {a.purpose ?? a.id} — {a.requestedAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Underwriting case (completed)" select fullWidth {...register('underwritingCaseId')} error={!!errors.underwritingCaseId} helperText={errors.underwritingCaseId?.message}>
            {completedCases.map((c) => (
              <MenuItem key={c.id} value={c.id}>
                {c.id} — {c.decision}
              </MenuItem>
            ))}
          </TextField>
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
