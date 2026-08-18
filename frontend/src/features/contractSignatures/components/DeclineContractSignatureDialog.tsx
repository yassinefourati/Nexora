import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { declineContractSignatureSchema, type DeclineContractSignatureFormData } from '../schemas/contractSignatureSchema';
import { useDeclineContractSignature } from '../hooks/useContractSignatures';
import LoadingButton from '@/shared/components/LoadingButton';
import type { ContractSignature } from '../api/contractSignaturesApi';

interface Props { open: boolean; onClose: () => void; contractSignature: ContractSignature | null; }

const emptyDefaults: DeclineContractSignatureFormData = { declineReason: '' };

export default function DeclineContractSignatureDialog({ open, onClose, contractSignature }: Props) {
  const { mutateAsync: decline, isPending } = useDeclineContractSignature();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<DeclineContractSignatureFormData>({
    resolver: zodResolver(declineContractSignatureSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: DeclineContractSignatureFormData) => {
    if (!contractSignature?.id) return;
    await decline({ id: contractSignature.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Decline Signature Request</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Decline reason" fullWidth multiline minRows={2} {...register('declineReason')} error={!!errors.declineReason} helperText={errors.declineReason?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" color="error" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Decline
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
