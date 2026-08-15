import { Box, Typography, Paper, Grid, Chip, Stack, Table, TableHead, TableBody, TableRow, TableCell, CircularProgress, Skeleton, Alert } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningIcon from '@mui/icons-material/Warning';
import ErrorIcon from '@mui/icons-material/Error';
import { useHealth } from '../hooks/useHealth';

const STATUS_COLOR: Record<string, 'success' | 'warning' | 'error'> = { UP: 'success', DEGRADED: 'warning', DOWN: 'error', OUT_OF_SERVICE: 'error' };
const STATUS_ICON: Record<string, typeof CheckCircleIcon> = { UP: CheckCircleIcon, DEGRADED: WarningIcon, DOWN: ErrorIcon, OUT_OF_SERVICE: ErrorIcon };

function formatDetails(details?: Record<string, unknown>): string {
  if (!details) return '—';
  return Object.entries(details).map(([k, v]) => `${k}: ${typeof v === 'object' ? JSON.stringify(v) : String(v)}`).join(' · ');
}

function SystemHealthSkeleton() {
  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
        <Skeleton variant="text" width={200} height={44} />
        <Skeleton variant="rounded" width={120} height={32} sx={{ borderRadius: 4 }} />
      </Stack>
      <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        <Box sx={{ p: 2 }}><Skeleton variant="text" width={120} height={28} /></Box>
        <Table>
          <TableBody>
            {Array.from({ length: 4 }).map((_, i) => (
              <TableRow key={i}>
                <TableCell><Skeleton variant="text" width={100} /></TableCell>
                <TableCell><Skeleton variant="rounded" width={90} height={24} sx={{ borderRadius: 4 }} /></TableCell>
                <TableCell><Skeleton variant="text" width={200} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Box>
  );
}

export default function SystemHealth() {
  const { data, isLoading, isFetching, isError, dataUpdatedAt } = useHealth();

  if (isLoading) return <SystemHealthSkeleton />;

  if (isError) {
    return (
      <Box>
        <Typography variant="h4" component="h1" fontWeight={700} mb={3}>System Health</Typography>
        <Alert severity="error">Could not reach the backend's health endpoint. The service may be down.</Alert>
      </Box>
    );
  }

  const overallOk = data?.status === 'UP';
  const components = Object.entries(data?.components ?? {});

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1" fontWeight={700}>System Health</Typography>
        <Stack direction="row" spacing={1} alignItems="center">
          {isFetching ? <CircularProgress size={16} aria-label="Refreshing" /> : null}
          <Chip label={data?.status ?? 'checking…'} color={overallOk ? 'success' : 'warning'} icon={overallOk ? <CheckCircleIcon /> : <WarningIcon />} />
          {dataUpdatedAt > 0 && (
            <Typography variant="caption" color="text.secondary">
              Updated {new Date(dataUpdatedAt).toLocaleTimeString()}
            </Typography>
          )}
        </Stack>
      </Stack>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12 }}>
          <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
            <Box sx={{ p: 2 }}><Typography variant="subtitle1" fontWeight={700}>Components</Typography></Box>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 700 }}>Component</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Status</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Details</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {components.map(([name, component]) => {
                  const Icon = STATUS_ICON[component.status] ?? WarningIcon;
                  return (
                    <TableRow key={name} hover>
                      <TableCell sx={{ fontWeight: 600 }}>{name}</TableCell>
                      <TableCell>
                        <Stack direction="row" spacing={0.5} alignItems="center">
                          <Icon fontSize="small" color={STATUS_COLOR[component.status] ?? 'warning'} />
                          <Chip label={component.status} size="small" color={STATUS_COLOR[component.status] ?? 'warning'} />
                        </Stack>
                      </TableCell>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.75rem' }}>{formatDetails(component.details)}</TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
