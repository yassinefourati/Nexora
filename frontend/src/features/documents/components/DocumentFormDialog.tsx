import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, MenuItem, Stack } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { documentSchema, type DocumentFormData } from '../schemas/documentSchema';
import { useCreateDocument } from '../hooks/useDocuments';
import LoadingButton from '@/shared/components/LoadingButton';

interface Props { open: boolean; onClose: () => void; }

const DOCUMENT_TYPE_OPTIONS = ['identity', 'proof_of_address', 'proof_of_income', 'employment_letter',
  'bank_statement', 'tax_document', 'credit_report', 'signed_contract', 'loan_offer', 'other'];
const CATEGORY_OPTIONS = ['identity', 'financial', 'legal', 'supporting'];

const emptyDefaults: DocumentFormData = {
  documentType: 'other',
  category: 'supporting',
  fileName: '',
  storageKey: '',
  contentType: '',
  sizeBytes: undefined,
};

export default function DocumentFormDialog({ open, onClose }: Props) {
  const { mutateAsync: create, isPending } = useCreateDocument();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<DocumentFormData>({
    resolver: zodResolver(documentSchema),
    defaultValues: emptyDefaults,
  });

  const onSubmit = async (data: DocumentFormData) => {
    await create({ ...data, contentType: data.contentType || undefined });
    reset(emptyDefaults);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Register Document</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Document type" select fullWidth {...register('documentType')} error={!!errors.documentType} helperText={errors.documentType?.message}>
            {DOCUMENT_TYPE_OPTIONS.map((t) => <MenuItem key={t} value={t}>{t.replace(/_/g, ' ')}</MenuItem>)}
          </TextField>
          <TextField label="Category" select fullWidth {...register('category')} error={!!errors.category} helperText={errors.category?.message}>
            {CATEGORY_OPTIONS.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
          </TextField>
          <TextField label="File name" fullWidth {...register('fileName')} error={!!errors.fileName} helperText={errors.fileName?.message} />
          <TextField label="Storage key" fullWidth {...register('storageKey')} error={!!errors.storageKey} helperText={errors.storageKey?.message || 'Object-storage reference (e.g. MinIO key) — the file itself is uploaded out of band.'} />
          <TextField label="Content type" fullWidth {...register('contentType')} error={!!errors.contentType} helperText={errors.contentType?.message} />
          <TextField label="Size (bytes)" type="number" fullWidth {...register('sizeBytes', { valueAsNumber: true })} error={!!errors.sizeBytes} helperText={errors.sizeBytes?.message} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>Cancel</Button>
        <LoadingButton variant="contained" onClick={handleSubmit(onSubmit)} loading={isPending}>
          Register
        </LoadingButton>
      </DialogActions>
    </Dialog>
  );
}
