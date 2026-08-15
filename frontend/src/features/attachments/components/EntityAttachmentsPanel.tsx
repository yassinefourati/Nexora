import { Box, Paper, Typography, Stack, TextField, Button, IconButton, Link } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { attachmentSchema, type AttachmentFormData } from '../schemas/attachmentSchema';
import { useAttachments, useCreateAttachment, useDeleteAttachment } from '../hooks/useAttachments';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';

interface Props { entityType: string; entityId: string; }

export default function EntityAttachmentsPanel({ entityType, entityId }: Props) {
  const { data: attachments, isLoading } = useAttachments(entityType, entityId);
  const { mutateAsync: create, isPending } = useCreateAttachment();
  const { mutate: remove } = useDeleteAttachment(entityType, entityId);
  const { confirm } = useConfirmStore();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<AttachmentFormData>({
    resolver: zodResolver(attachmentSchema),
    defaultValues: { fileName: '', fileUrl: '', mimeType: '' },
  });

  const onSubmit = async (data: AttachmentFormData) => {
    await create({ entityType, entityId, ...data });
    reset();
  };

  return (
    <Paper variant="outlined" sx={{ p: 2, borderRadius: 2 }}>
      <Typography variant="subtitle2" fontWeight={700} mb={1.5}>Attachments</Typography>
      {isLoading ? (
        <Typography variant="body2" color="text.secondary">Loading…</Typography>
      ) : !attachments?.length ? (
        <Typography variant="body2" color="text.secondary" mb={2}>No attachments yet.</Typography>
      ) : (
        <Stack spacing={1} mb={2}>
          {attachments.map((a) => (
            <Box key={a.id} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <AttachFileIcon fontSize="small" color="action" />
              <Link href={a.fileUrl} target="_blank" rel="noopener" sx={{ flexGrow: 1 }}>{a.fileName}</Link>
              <Typography variant="caption" color="text.secondary">{a.mimeType}</Typography>
              <IconButton size="small" onClick={() => confirm({
                title: 'Remove attachment', message: `Remove "${a.fileName}"?`, confirmLabel: 'Remove', severity: 'error',
                onConfirm: () => remove(a.id!),
              })}>
                <DeleteIcon fontSize="small" color="error" />
              </IconButton>
            </Box>
          ))}
        </Stack>
      )}
      <Stack direction="row" spacing={1} flexWrap="wrap">
        <TextField size="small" label="File name" {...register('fileName')} error={!!errors.fileName} helperText={errors.fileName?.message} />
        <TextField size="small" label="File URL" {...register('fileUrl')} error={!!errors.fileUrl} helperText={errors.fileUrl?.message} sx={{ minWidth: 220 }} />
        <TextField size="small" label="MIME type" {...register('mimeType')} />
        <Button variant="contained" onClick={handleSubmit(onSubmit)} disabled={isPending}>Add</Button>
      </Stack>
    </Paper>
  );
}
