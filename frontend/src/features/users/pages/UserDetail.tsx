import { Box, Typography, Paper, Avatar, Chip, Grid, Button } from '@mui/material';
import DetailSkeleton from '@/shared/components/skeletons/DetailSkeleton';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useParams, useNavigate } from 'react-router-dom';
import { useUser } from '../hooks/useUsers';
import { ROUTES } from '@/core/router/routes';
import EntityCommentsPanel from '@/features/comments/components/EntityCommentsPanel';
import EntityAttachmentsPanel from '@/features/attachments/components/EntityAttachmentsPanel';

export default function UserDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: user, isLoading } = useUser(id);

  if (isLoading) return <DetailSkeleton />;
  if (!user) return <Box sx={{ p: 4 }}><Typography>User not found.</Typography></Box>;

  const fullName = [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username;

  return (
    <Box>
      <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(ROUTES.USERS)} sx={{ mb: 2 }}>Back to Users</Button>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Paper elevation={2} sx={{ p: 3, borderRadius: 3, textAlign: 'center' }}>
            <Avatar sx={{ width: 80, height: 80, bgcolor: 'primary.main', fontSize: '2rem', mx: 'auto', mb: 2 }}>
              {fullName?.[0]?.toUpperCase()}
            </Avatar>
            <Typography variant="h6" fontWeight={700}>{fullName}</Typography>
            <Typography variant="body2" color="text.secondary" mb={1}>{user.email}</Typography>
            <Chip label={user.status} color={user.status === 'active' ? 'success' : 'default'} size="small" />
            {user.superuser && <Chip label="Superuser" color="error" size="small" variant="outlined" sx={{ ml: 1 }} />}
          </Paper>
        </Grid>

        <Grid size={{ xs: 12, md: 8 }}>
          <Paper elevation={2} sx={{ p: 3, borderRadius: 3 }}>
            <Typography variant="subtitle1" fontWeight={700} mb={2}>Account details</Typography>
            <Grid container spacing={2}>
              <Grid size={6}><Typography variant="caption" color="text.secondary">Username</Typography><Typography>{user.username}</Typography></Grid>
              <Grid size={6}><Typography variant="caption" color="text.secondary">Failed login attempts</Typography><Typography>{user.failedLoginAttempts ?? 0}</Typography></Grid>
              <Grid size={6}><Typography variant="caption" color="text.secondary">Last login</Typography><Typography>{user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : '—'}</Typography></Grid>
              <Grid size={6}><Typography variant="caption" color="text.secondary">Locked until</Typography><Typography>{user.lockedUntil ? new Date(user.lockedUntil).toLocaleString() : '—'}</Typography></Grid>
              <Grid size={6}><Typography variant="caption" color="text.secondary">Password changed</Typography><Typography>{user.passwordChangedAt ? new Date(user.passwordChangedAt).toLocaleString() : '—'}</Typography></Grid>
              <Grid size={6}><Typography variant="caption" color="text.secondary">Created</Typography><Typography>{user.createdAt ? new Date(user.createdAt).toLocaleString() : '—'}</Typography></Grid>
            </Grid>
          </Paper>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <EntityCommentsPanel entityType="user" entityId={user.id!} />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <EntityAttachmentsPanel entityType="user" entityId={user.id!} />
        </Grid>
      </Grid>
    </Box>
  );
}
