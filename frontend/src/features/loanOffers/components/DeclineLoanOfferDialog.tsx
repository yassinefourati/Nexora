import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { declineLoanOfferSchema, type DeclineLoanOfferFormData } from '../schemas/loanOfferSchema';
import { useDeclineLoanOffer } from '../hooks/useLoanOffers';
import LoadingButton from '@/shared/components/LoadingButton';
import type { LoanOffer } from '../api/loanOffersApi';

interface Props { open: boolean; onClose: () => void; loanOffer: LoanOffer | null; }

const emptyDefaults: DeclineLoanOfferFormData = { declineReason: '' };

export default function DeclineLoanOfferDialog({ open, onClose, loanOffer }: Props) {
  const { mutateAsync: decline, isPending } = useDeclineLoanOffer();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<DeclineLoanOfferFormData>({
    resolver: zodResolver(declineLoanOfferSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: DeclineLoanOfferFormData) => {
    if (!loanOffer?.id) return;
    await decline({ id: loanOffer.id, body: data });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Decline Loan Offer</DialogTitle>
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
