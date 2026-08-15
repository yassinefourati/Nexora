import { Skeleton, Stack, Box, Paper } from '@mui/material';
export default function TableSkeleton({ rows = 5, showToolbar = true }: { rows?: number; showToolbar?: boolean }) {
  return (
    <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
      {showToolbar && (
        <Stack direction="row" spacing={1} alignItems="center" sx={{ px: 2, py: 1.5, borderBottom: '1px solid', borderColor: 'divider' }}>
          <Skeleton variant="text" width={120} height={28} />
          <Box sx={{ flex: 1 }} />
          <Skeleton variant="rounded" width={160} height={32} />
          <Skeleton variant="rounded" width={90} height={32} />
          <Skeleton variant="rounded" width={90} height={32} />
        </Stack>
      )}
      {/* Header row */}
      <Stack direction="row" spacing={2} sx={{ px: 2, py: 1, bgcolor: 'action.hover' }}>
        <Skeleton variant="rounded" width={20} height={20} />
        {[35, 20, 15, 10].map((w, i) => (
          <Skeleton key={i} variant="text" width={`${w}%`} height={20} />
        ))}
      </Stack>
      {/* Data rows */}
      {Array.from({ length: rows }).map((_, i) => (
        <Stack key={i} direction="row" spacing={2} alignItems="center"
          sx={{ px: 2, py: 1.2, borderTop: '1px solid', borderColor: 'divider', opacity: 1 - i * 0.07 }}>
          <Skeleton variant="rounded" width={20} height={20} />
          <Skeleton variant="circular" width={32} height={32} />
          <Box sx={{ flex: 1 }}>
            <Skeleton variant="text" width="50%" height={18} />
            <Skeleton variant="text" width="30%" height={14} />
          </Box>
          <Skeleton variant="rounded" width="15%" height={18} />
          <Skeleton variant="rounded" width={60} height={22} sx={{ borderRadius: 4 }} />
          <Stack direction="row" spacing={0.5}>
            <Skeleton variant="circular" width={28} height={28} />
            <Skeleton variant="circular" width={28} height={28} />
          </Stack>
        </Stack>
      ))}
      {/* Footer pagination */}
      <Stack direction="row" justifyContent="flex-end" spacing={2} alignItems="center"
        sx={{ px: 2, py: 1, borderTop: '1px solid', borderColor: 'divider' }}>
        <Skeleton variant="text" width={100} height={20} />
        <Skeleton variant="rounded" width={80} height={28} />
      </Stack>
    </Paper>
  );
}
