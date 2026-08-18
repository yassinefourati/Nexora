import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { contractSignatureSchema, SIGNER_ROLE_OPTIONS, SIGNATURE_METHOD_OPTIONS, type ContractSignatureFormData } from '../schemas/contractSignatureSchema';
import { useCreateContractSignature } from '../hooks/useContractSignatures';
import { useLoanContracts } from '@/features/loanContracts/hooks/useLoanContracts';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

const emptyDefaults: ContractSignatureFormData = {
  loanContractId: '',
  signerName: '',
  signerRole: 'primary_applicant',
  signatureMethod: 'electronic',
};

export default function ContractSignatureFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateContractSignature();
  const { data: loanContractsPage } = useLoanContracts({ page: 0, size: 100 });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<ContractSignatureFormData>({
    resolver: zodResolver(contractSignatureSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: ContractSignatureFormData) => {
    await create(data);
    reset(emptyDefaults);
    onClose();
  };

  const finalizedContracts = (loanContractsPage?.items ?? []).filter((c) => c.status === 'finalized');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Request Signature</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Loan contract (finalized)" select fullWidth {...register('loanContractId')} error={!!errors.loanContractId} helperText={errors.loanContractId?.message}>
            {finalizedContracts.map((c) => (
              <MenuItem key={c.id} value={c.id}>
                {c.contractNumber} — {c.principalAmount}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Signer name" fullWidth {...register('signerName')} error={!!errors.signerName} helperText={errors.signerName?.message} />
          <TextField label="Signer role" select fullWidth {...register('signerRole')} error={!!errors.signerRole} helperText={errors.signerRole?.message}>
            {SIGNER_ROLE_OPTIONS.map((r) => <MenuItem key={r} value={r}>{r.replace(/_/g, ' ')}</MenuItem>)}
          </TextField>
          <TextField label="Signature method" select fullWidth {...register('signatureMethod')} error={!!errors.signatureMethod} helperText={errors.signatureMethod?.message}>
            {SIGNATURE_METHOD_OPTIONS.map((m) => <MenuItem key={m} value={m}>{m.replace('_', ' ')}</MenuItem>)}
          </TextField>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Request
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
