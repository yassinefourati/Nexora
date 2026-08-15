import { Grid, Typography, Box, Paper, List, ListItem, ListItemText, Chip } from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import ApartmentIcon from '@mui/icons-material/Apartment';
import ShieldIcon from '@mui/icons-material/Shield';
import DevicesIcon from '@mui/icons-material/Devices';
import { useTranslation } from 'react-i18next';
import { useUserCount, useOrganizationCount, useRoleCount, useActiveSessionCount } from '../hooks/useDashboard';
import { useAuditLog } from '@/features/audit/hooks/useAudit';
import StatCard from '../components/StatCard';
import SectionErrorBoundary from '@/shared/components/SectionErrorBoundary';
import PageHeader from '@/shared/components/PageHeader';

export default function Dashboard() {
  const { t } = useTranslation();
  const { data: userCount, isLoading: userLoading } = useUserCount();
  const { data: orgCount, isLoading: orgLoading } = useOrganizationCount();
  const { data: roleCount, isLoading: roleLoading } = useRoleCount();
  const { data: sessionCount, isLoading: sessionLoading } = useActiveSessionCount();
  const { data: recentAudit, isLoading: auditLoading } = useAuditLog({ page: 0, size: 8, sort: 'createdAt,desc' });

  return (
    <Box>
      <PageHeader title={t('dashboard.title')} description="An overview of your workspace — live counts from the backend." />

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
          <StatCard title="Users" value={userCount ?? 0} icon={<PeopleIcon />} color="#1976d2" loading={userLoading} />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
          <StatCard title="Organizations" value={orgCount ?? 0} icon={<ApartmentIcon />} color="#2e7d32" loading={orgLoading} />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
          <StatCard title="Roles" value={roleCount ?? 0} icon={<ShieldIcon />} color="#ed6c02" loading={roleLoading} />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
          <StatCard title="Active Sessions" value={sessionCount ?? 0} icon={<DevicesIcon />} color="#9c27b0" loading={sessionLoading} />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12 }}>
          <SectionErrorBoundary>
            <Paper variant="outlined" sx={{ p: 3, borderRadius: 3 }}>
              <Typography variant="subtitle1" component="h2" fontWeight={700} mb={1.5}>Recent Activity</Typography>
              {auditLoading ? (
                <Typography variant="body2" color="text.secondary">Loading…</Typography>
              ) : !recentAudit?.items.length ? (
                <Typography variant="body2" color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>No recent activity to show.</Typography>
              ) : (
                <List disablePadding dense>
                  {recentAudit.items.map((e) => (
                    <ListItem key={e.id} disablePadding sx={{ py: 0.75 }}>
                      <ListItemText
                        primary={<>
                          <Chip label={e.action} size="small" variant="outlined" sx={{ mr: 1 }} />
                          {e.entityType}
                        </>}
                        secondary={e.createdAt ? new Date(e.createdAt).toLocaleString() : ''}
                        primaryTypographyProps={{ fontSize: '0.85rem' }}
                        secondaryTypographyProps={{ fontSize: '0.72rem' }}
                      />
                    </ListItem>
                  ))}
                </List>
              )}
            </Paper>
          </SectionErrorBoundary>
        </Grid>
      </Grid>
    </Box>
  );
}
