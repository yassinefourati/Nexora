import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { resolveCollectionCaseSchema, type ResolveCollectionCaseFormData } from '../schemas/collectionCaseSchema';
import { useResolveCollectionCase } from '../hooks/useCollectionCases';
import LoadingButton from '@/shared/components/LoadingButton';
import type { CollectionCase } from '../api/collectionCasesApi';

interface Props { open: boolean; onClose: () => void; collectionCase: CollectionCase | null; }

const emptyDefaults: ResolveCollectionCaseFormData = { resolutionNotes: '' };

export default function ResolveCollectionCaseDialog({ open, onClose, collectionCase }: Props) {
  const { mutateAsync: resolve, isPending } = useResolveCollectionCase();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<ResolveCollectionCaseFormData>({
    resolver: zodResolver(resolveCollectionCaseSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: ResolveCollectionCaseFormData) => {
    if (!collectionCase?.id) return;
    await resolve({ id: collectionCase.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Resolve Collection Case</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Resolution notes" fullWidth multiline minRows={2} {...register('resolutionNotes')} error={!!errors.resolutionNotes} helperText={errors.resolutionNotes?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Resolve
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
