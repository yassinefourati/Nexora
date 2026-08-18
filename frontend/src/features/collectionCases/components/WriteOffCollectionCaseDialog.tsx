import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { writeOffCollectionCaseSchema, type WriteOffCollectionCaseFormData } from '../schemas/collectionCaseSchema';
import { useWriteOffCollectionCase } from '../hooks/useCollectionCases';
import LoadingButton from '@/shared/components/LoadingButton';
import type { CollectionCase } from '../api/collectionCasesApi';

interface Props { open: boolean; onClose: () => void; collectionCase: CollectionCase | null; }

const emptyDefaults: WriteOffCollectionCaseFormData = { resolutionNotes: '' };

export default function WriteOffCollectionCaseDialog({ open, onClose, collectionCase }: Props) {
  const { mutateAsync: writeOff, isPending } = useWriteOffCollectionCase();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<WriteOffCollectionCaseFormData>({
    resolver: zodResolver(writeOffCollectionCaseSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: WriteOffCollectionCaseFormData) => {
    if (!collectionCase?.id) return;
    await writeOff({ id: collectionCase.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Write Off Collection Case</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Resolution notes" fullWidth multiline minRows={2} {...register('resolutionNotes')} error={!!errors.resolutionNotes} helperText={errors.resolutionNotes?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" color="error" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Write off
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
