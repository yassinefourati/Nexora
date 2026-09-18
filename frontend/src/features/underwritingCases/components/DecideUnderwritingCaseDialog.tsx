import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { decideUnderwritingCaseSchema, DECISION_OPTIONS, type DecideUnderwritingCaseFormData } from '../schemas/underwritingCaseSchema';
import { useDecideUnderwritingCase } from '../hooks/useUnderwritingCases';
import LoadingButton from '@/shared/components/LoadingButton';
import type { UnderwritingCase } from '../api/underwritingCasesApi';

interface Props { open: boolean; onClose: () => void; underwritingCase: UnderwritingCase | null; }

const emptyDefaults: DecideUnderwritingCaseFormData = {
  decision: 'approve',
  decisionReason: '',
};

export default function DecideUnderwritingCaseDialog({ open, onClose, underwritingCase }: Props) {
  const { mutateAsync: decide, isPending } = useDecideUnderwritingCase();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<DecideUnderwritingCaseFormData>({
    resolver: zodResolver(decideUnderwritingCaseSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: DecideUnderwritingCaseFormData) => {
    if (!underwritingCase?.id) return;
    await decide({ id: underwritingCase.id, body: { ...data, decisionReason: data.decisionReason || undefined } });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Record Underwriting Decision</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Decision" select fullWidth {...register('decision')} error={!!errors.decision} helperText={errors.decision?.message}>
            {DECISION_OPTIONS.map((d) => <MenuItem key={d} value={d}>{d.replace(/_/g, ' ')}</MenuItem>)}
          </TextField>
          <TextField label="Reason" fullWidth multiline minRows={2} {...register('decisionReason')} error={!!errors.decisionReason} helperText={errors.decisionReason?.message} />
          <TextField label="Approved amount" type="number" fullWidth {...register('approvedAmount', { valueAsNumber: true })} error={!!errors.approvedAmount} helperText={errors.approvedAmount?.message} />
          <TextField label="Approved term (months)" type="number" fullWidth {...register('approvedTermMonths', { valueAsNumber: true })} error={!!errors.approvedTermMonths} helperText={errors.approvedTermMonths?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Save decision
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
