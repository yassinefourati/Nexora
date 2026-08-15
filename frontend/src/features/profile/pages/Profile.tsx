import { Box, Typography, Paper, Avatar, Chip, Stack, List, ListItem, ListItemText, ListItemSecondaryAction, IconButton, Divider, Button, Alert } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import DevicesIcon from '@mui/icons-material/Devices';
import VpnKeyIcon from '@mui/icons-material/VpnKey';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import { useCurrentBackendUser } from '@/core/auth/useCurrentBackendUser';
import { useAuthStore } from '@/core/auth/stores/useAuthStore';
import { useSessionsList, useRevokeSession } from '@/features/sessions/hooks/useSessions';
import { useApiKeysList, useRevokeApiKey } from '@/features/apiKeys/hooks/useApiKeys';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import ProfileSkeleton from '@/shared/components/skeletons/ProfileSkeleton';
import { env } from '@/core/config/env';

const accountConsoleUrl = `${env.VITE_OIDC_AUTHORITY}/account/`;

export default function Profile() {
  const jwtUser = useAuthStore((s) => s.user);
  const { data: profile, isLoading, isError } = useCurrentBackendUser();
  const { data: sessionsPage } = useSessionsList({ page: 0, size: 20, userId: profile?.id });
  const { data: apiKeysPage } = useApiKeysList({ page: 0, size: 20, userId: profile?.id });
  const { mutate: revokeSession } = useRevokeSession();
  const { mutate: revokeApiKey } = useRevokeApiKey();
  const { confirm } = useConfirmStore();

  if (isLoading) return <ProfileSkeleton />;

  const fullName = [profile?.firstName, profile?.lastName].filter(Boolean).join(' ') || jwtUser?.username;
  const sessions = sessionsPage?.items ?? [];
  const apiKeys = apiKeysPage?.items ?? [];

  return (
    <Box>
      <Stack direction="row" spacing={2} alignItems="center" mb={3}>
        <Avatar sx={{ width: 64, height: 64, bgcolor: 'primary.main', fontSize: '1.5rem' }}>{fullName?.[0]?.toUpperCase()}</Avatar>
        <Box>
          <Typography variant="h4" component="h1" fontWeight={700}>{fullName}</Typography>
          <Stack direction="row" spacing={1} mt={0.5}>
            {jwtUser?.roles.map((r) => <Chip key={r} label={r} size="small" color="primary" variant="outlined" />)}
            {profile && <Chip label={profile.status} size="small" color={profile.status === 'active' ? 'success' : 'default'} />}
          </Stack>
        </Box>
      </Stack>

      {isError && (
        <Alert severity="error" sx={{ mb: 3 }}>
          Couldn't load your account details. Try refreshing the page.
        </Alert>
      )}
      {!isError && !profile && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          No matching backend user record found for "{jwtUser?.username}" — sessions and API keys below can't be scoped to this account.
        </Alert>
      )}

      <Stack spacing={3} maxWidth={640}>
        <Paper elevation={2} sx={{ p: 3, borderRadius: 3 }}>
          <Typography variant="h6" mb={2}>Account details</Typography>
          <Stack spacing={1.5}>
            <Box><Typography variant="caption" color="text.secondary">Username</Typography><Typography>{profile?.username ?? jwtUser?.username}</Typography></Box>
            <Box><Typography variant="caption" color="text.secondary">Email</Typography><Typography>{profile?.email ?? jwtUser?.email}</Typography></Box>
            <Box><Typography variant="caption" color="text.secondary">Last login</Typography><Typography>{profile?.lastLoginAt ? new Date(profile.lastLoginAt).toLocaleString() : '—'}</Typography></Box>
          </Stack>
        </Paper>

        <Paper elevation={2} sx={{ p: 3, borderRadius: 3 }}>
          <Typography variant="h6" mb={1}>Password &amp; two-factor authentication</Typography>
          <Typography variant="body2" color="text.secondary" mb={2}>
            Sign-in is managed by the identity provider — change your password, set up two-factor authentication,
            or review your login devices in the account console.
          </Typography>
          <Button variant="outlined" endIcon={<OpenInNewIcon fontSize="small" />} href={accountConsoleUrl} target="_blank" rel="noopener">
            Open account console
          </Button>
        </Paper>

        <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
          <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
            <DevicesIcon fontSize="small" color="action" />
            <Typography variant="subtitle1" fontWeight={600}>Your sessions</Typography>
          </Box>
          <Divider />
          {sessions.length === 0 ? (
            <Box sx={{ p: 3, textAlign: 'center' }}><Typography variant="body2" color="text.secondary">No sessions found.</Typography></Box>
          ) : (
            <List disablePadding>
              {sessions.map((s) => (
                <ListItem key={s.id} divider>
                  <ListItemText
                    primary={<>{s.userAgent ?? 'Unknown device'} {s.revokedAt && <Chip label="revoked" size="small" sx={{ ml: 1 }} />}</>}
                    secondary={`${s.ipAddress ?? '—'} · expires ${s.expiresAt ? new Date(s.expiresAt).toLocaleString() : '—'}`}
                  />
                  {!s.revokedAt && (
                    <ListItemSecondaryAction>
                      <IconButton size="small" color="error" aria-label="Revoke session" onClick={() => confirm({
                        title: 'Revoke session', message: 'This device will be signed out immediately.', severity: 'error',
                        onConfirm: () => revokeSession(s.id!),
                      })}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </ListItemSecondaryAction>
                  )}
                </ListItem>
              ))}
            </List>
          )}
        </Paper>

        <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
          <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
            <VpnKeyIcon fontSize="small" color="action" />
            <Typography variant="subtitle1" fontWeight={600}>Your API keys</Typography>
          </Box>
          <Divider />
          {apiKeys.length === 0 ? (
            <Box sx={{ p: 3, textAlign: 'center' }}><Typography variant="body2" color="text.secondary">No API keys issued to this account.</Typography></Box>
          ) : (
            <List disablePadding>
              {apiKeys.map((k) => (
                <ListItem key={k.id} divider>
                  <ListItemText
                    primary={<>{k.name} {k.revokedAt && <Chip label="revoked" size="small" sx={{ ml: 1 }} />}</>}
                    secondary={`Last used ${k.lastUsedAt ? new Date(k.lastUsedAt).toLocaleString() : 'never'}`}
                  />
                  {!k.revokedAt && (
                    <ListItemSecondaryAction>
                      <IconButton size="small" color="error" aria-label="Revoke API key" onClick={() => confirm({
                        title: 'Revoke API key', message: `Revoke "${k.name}"? Requests using it will be rejected immediately.`, severity: 'error',
                        onConfirm: () => revokeApiKey(k.id!),
                      })}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </ListItemSecondaryAction>
                  )}
                </ListItem>
              ))}
            </List>
          )}
        </Paper>
      </Stack>
    </Box>
  );
}
