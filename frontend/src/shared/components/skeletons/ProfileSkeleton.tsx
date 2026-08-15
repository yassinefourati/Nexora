import { Box, Paper, Skeleton, Stack } from '@mui/material';
export default function ProfileSkeleton() {
  return (
    <Box>
      {/* Avatar + name */}
      <Stack direction="row" spacing={2} alignItems="center" mb={3}>
        <Skeleton variant="circular" width={64} height={64} />
        <Box>
          <Skeleton variant="text" width={180} height={36} />
          <Skeleton variant="rounded" width={80} height={22} sx={{ borderRadius: 4 }} />
        </Box>
      </Stack>
      {/* Tabs */}
      <Stack direction="row" spacing={1} sx={{ mb: 3, borderBottom: '1px solid', borderColor: 'divider', pb: 0 }}>
        {[1,2,3,4].map((i) => <Skeleton key={i} variant="rounded" width={80} height={36} />)}
      </Stack>
      {/* Form */}
      <Paper elevation={2} sx={{ p: 3, borderRadius: 3, maxWidth: 500 }}>
        <Stack spacing={2.5}>
          {[1,2,3,4].map((i) => <Skeleton key={i} variant="rounded" height={56} />)}
          <Skeleton variant="rounded" width={120} height={40} />
        </Stack>
      </Paper>
    </Box>
  );
}
