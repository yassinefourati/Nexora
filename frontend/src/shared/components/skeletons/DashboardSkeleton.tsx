import { Grid, Skeleton, Box, Paper, Stack } from '@mui/material';
export default function DashboardSkeleton() {
  return (
    <Box>
      {/* Title + online badge */}
      <Stack direction="row" spacing={1} alignItems="center" mb={3}>
        <Skeleton variant="text" width={180} height={44} />
        <Skeleton variant="rounded" width={80} height={24} sx={{ borderRadius: 4 }} />
      </Stack>
      {/* Stat cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {[1,2,3,4].map((i) => (
          <Grid key={i} size={{ xs:12, sm:6, xl:3 }}>
            <Paper elevation={2} sx={{ p: 3, borderRadius: 3, display:'flex', alignItems:'center', gap: 2 }}>
              <Skeleton variant="rounded" width={48} height={48} />
              <Box sx={{ flex: 1 }}>
                <Skeleton variant="text" width="60%" height={18} />
                <Skeleton variant="text" width="40%" height={32} />
              </Box>
            </Paper>
          </Grid>
        ))}
      </Grid>
      {/* Charts row */}
      <Grid container spacing={3}>
        <Grid size={{ xs:12, md:7 }}>
          <Paper elevation={2} sx={{ p: 3, borderRadius: 3 }}>
            <Skeleton variant="text" width={180} height={24} sx={{ mb: 2 }} />
            <Skeleton variant="rounded" height={180} />
          </Paper>
        </Grid>
        <Grid size={{ xs:12, md:5 }}>
          <Paper elevation={2} sx={{ p: 3, borderRadius: 3 }}>
            <Skeleton variant="text" width={140} height={24} sx={{ mb: 2 }} />
            {[1,2,3,4,5].map((i) => (
              <Stack key={i} direction="row" spacing={1.5} alignItems="center" sx={{ mb: 1 }}>
                <Skeleton variant="circular" width={28} height={28} />
                <Box sx={{ flex: 1 }}>
                  <Skeleton variant="text" height={18} />
                </Box>
              </Stack>
            ))}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
