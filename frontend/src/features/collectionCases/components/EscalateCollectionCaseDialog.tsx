import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { escalateCollectionCaseSchema, STAGE_OPTIONS, type EscalateCollectionCaseFormData } from '../schemas/collectionCaseSchema';
import { useEscalateCollectionCase } from '../hooks/useCollectionCases';
import LoadingButton from '@/shared/components/LoadingButton';
import type { CollectionCase } from '../api/collectionCasesApi';

interface Props { open: boolean; onClose: () => void; collectionCase: CollectionCase | null; }

const emptyDefaults: EscalateCollectionCaseFormData = { stage: 'notice' };

export default function EscalateCollectionCaseDialog({ open, onClose, collectionCase }: Props) {
  const { mutateAsync: escalate, isPending } = useEscalateCollectionCase();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<EscalateCollectionCaseFormData>({
    resolver: zodResolver(escalateCollectionCaseSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: EscalateCollectionCaseFormData) => {
    if (!collectionCase?.id) return;
    await escalate({ id: collectionCase.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Escalate Collection Case</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Stage" select fullWidth {...register('stage')} error={!!errors.stage} helperText={errors.stage?.message}>
            {STAGE_OPTIONS.map((s) => <MenuItem key={s} value={s}>{s.replace(/_/g, ' ')}</MenuItem>)}
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Escalate
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
