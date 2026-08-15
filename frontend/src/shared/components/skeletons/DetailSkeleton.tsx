import { Box, Paper, Skeleton, Stack, Grid, Divider } from '@mui/material';
export default function DetailSkeleton() {
  return (
    <Box>
      <Skeleton variant="rounded" width={120} height={32} sx={{ mb: 3 }} />
      <Grid container spacing={3}>
        <Grid size={{ xs:12, md:4 }}>
          <Paper elevation={2} sx={{ p: 3, borderRadius: 3, textAlign: 'center' }}>
            <Skeleton variant="circular" width={80} height={80} sx={{ mx: 'auto', mb: 2 }} />
            <Skeleton variant="text" width="70%" sx={{ mx: 'auto' }} height={28} />
            <Skeleton variant="text" width="50%" sx={{ mx: 'auto' }} height={20} />
            <Skeleton variant="rounded" width={80} height={24} sx={{ mx: 'auto', mt: 1, borderRadius: 4 }} />
          </Paper>
        </Grid>
        <Grid size={{ xs:12, md:8 }}>
          <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
            <Box sx={{ p: 2 }}><Skeleton variant="text" width={180} height={28} /></Box>
            <Divider />
            {[1,2,3,4,5].map((i) => (
              <Stack key={i} direction="row" spacing={2} sx={{ px: 2, py: 1.5, borderTop: i > 1 ? '1px solid' : 'none', borderColor: 'divider' }}>
                <Skeleton variant="text" width="25%" />
                <Skeleton variant="text" width="35%" />
                <Skeleton variant="rounded" width={60} height={22} sx={{ borderRadius: 4 }} />
              </Stack>
            ))}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
