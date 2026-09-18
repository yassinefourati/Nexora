import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { underwritingCaseSchema, type UnderwritingCaseFormData } from '../schemas/underwritingCaseSchema';
import { useCreateUnderwritingCase } from '../hooks/useUnderwritingCases';
import { useLoanApplications } from '@/features/loanApplications/hooks/useLoanApplications';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

const emptyDefaults: UnderwritingCaseFormData = {
  loanApplicationId: '',
  assignedTo: '',
};

export default function UnderwritingCaseFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateUnderwritingCase();
  const { data: loanApplicationsPage } = useLoanApplications({ page: 0, size: 100 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<UnderwritingCaseFormData>({
    resolver: zodResolver(underwritingCaseSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: UnderwritingCaseFormData) => {
    await create({ ...data, assignedTo: data.assignedTo || undefined });
    reset(emptyDefaults);
    onClose();
  };

  const loanApplications = loanApplicationsPage?.items ?? [];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Open Underwriting Case</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Loan application" select fullWidth {...register('loanApplicationId')} error={!!errors.loanApplicationId} helperText={errors.loanApplicationId?.message}>
            {loanApplications.map((a) => (
              <MenuItem key={a.id} value={a.id}>
                {a.purpose ?? a.id} — {a.requestedAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Assigned to" fullWidth {...register('assignedTo')} error={!!errors.assignedTo} helperText={errors.assignedTo?.message} />
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
