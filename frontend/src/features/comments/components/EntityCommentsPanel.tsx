import { useState } from 'react';
import { Box, Paper, Typography, Stack, TextField, Button, Divider, IconButton } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { useComments, useCreateComment, useDeleteComment } from '../hooks/useComments';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';

interface Props { entityType: string; entityId: string; }

export default function EntityCommentsPanel({ entityType, entityId }: Props) {
  const { data, isLoading } = useComments(entityType, entityId, { page: 0, size: 50 });
  const { mutate: create, isPending } = useCreateComment();
  const { mutate: remove } = useDeleteComment(entityType, entityId);
  const { confirm } = useConfirmStore();
  const [body, setBody] = useState('');

  const comments = data?.items ?? [];

  const submit = () => {
    if (!body.trim()) return;
    create({ entityType, entityId, body: body.trim() }, { onSuccess: () => setBody('') });
  };

  return (
    <Paper variant="outlined" sx={{ p: 2, borderRadius: 2 }}>
      <Typography variant="subtitle2" fontWeight={700} mb={1.5}>Comments</Typography>
      {isLoading ? (
        <Typography variant="body2" color="text.secondary">Loading…</Typography>
      ) : comments.length === 0 ? (
        <Typography variant="body2" color="text.secondary">No comments yet.</Typography>
      ) : (
        <Stack divider={<Divider />} spacing={1.5} mb={2}>
          {comments.map((c) => (
            <Box key={c.id} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1 }}>
              <Box>
                <Typography variant="body2">{c.body}</Typography>
                <Typography variant="caption" color="text.secondary">{c.createdAt ? new Date(c.createdAt).toLocaleString() : ''}</Typography>
              </Box>
              <IconButton size="small" onClick={() => confirm({
                title: 'Delete comment', message: 'Delete this comment?', confirmLabel: 'Delete', severity: 'error',
                onConfirm: () => remove(c.id!),
              })}>
                <DeleteIcon fontSize="small" color="error" />
              </IconButton>
            </Box>
          ))}
        </Stack>
      )}
      <Stack direction="row" spacing={1}>
        <TextField size="small" fullWidth placeholder="Add a comment…" value={body} onChange={(e) => setBody(e.target.value)} />
        <Button variant="contained" onClick={submit} disabled={isPending || !body.trim()}>Post</Button>
      </Stack>
    </Paper>
  );
}
