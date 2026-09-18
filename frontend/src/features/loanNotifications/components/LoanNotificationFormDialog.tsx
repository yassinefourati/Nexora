import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loanNotificationSchema, EVENT_TYPE_OPTIONS, CHANNEL_OPTIONS, type LoanNotificationFormData } from '../schemas/loanNotificationSchema';
import { useCreateLoanNotification } from '../hooks/useLoanNotifications';
import { useLoanApplications } from '@/features/loanApplications/hooks/useLoanApplications';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

const emptyDefaults: LoanNotificationFormData = {
  loanApplicationId: '',
  eventType: 'application_submitted',
  title: '',
  body: '',
  channel: 'email',
};

export default function LoanNotificationFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateLoanNotification();
  const { data: loanApplicationsPage } = useLoanApplications({ page: 0, size: 100 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<LoanNotificationFormData>({
    resolver: zodResolver(loanNotificationSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: LoanNotificationFormData) => {
    await create(data);
    reset(emptyDefaults);
    onClose();
  };

  const loanApplications = loanApplicationsPage?.items ?? [];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Send Loan Notification</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Loan application" select fullWidth {...register('loanApplicationId')} error={!!errors.loanApplicationId} helperText={errors.loanApplicationId?.message}>
            {loanApplications.map((a) => (
              <MenuItem key={a.id} value={a.id}>
                {a.purpose ?? a.id} — {a.requestedAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Event type" select fullWidth {...register('eventType')} error={!!errors.eventType} helperText={errors.eventType?.message}>
            {EVENT_TYPE_OPTIONS.map((e) => <MenuItem key={e} value={e}>{e.replace(/_/g, ' ')}</MenuItem>)}
          </TextField>
          <TextField label="Title" fullWidth {...register('title')} error={!!errors.title} helperText={errors.title?.message} />
          <TextField label="Body" fullWidth multiline minRows={3} {...register('body')} error={!!errors.body} helperText={errors.body?.message} />
          <TextField label="Channel" select fullWidth {...register('channel')} error={!!errors.channel} helperText={errors.channel?.message}>
            {CHANNEL_OPTIONS.map((c) => <MenuItem key={c} value={c}>{c.replace('_', ' ')}</MenuItem>)}
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Send
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
