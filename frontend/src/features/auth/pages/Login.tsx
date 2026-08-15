import { Box, Button, Typography, Paper, CircularProgress } from '@mui/material';
import DashboardRoundedIcon from '@mui/icons-material/DashboardRounded';
import LoginRoundedIcon from '@mui/icons-material/LoginRounded';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '@/core/auth/stores/useAuthStore';

/**
 * Real auth model: the backend is a stateless JWT resource server with no
 * password-login endpoint. Sign-in is delegated entirely to Keycloak via
 * OIDC Authorization Code + PKCE — this page just redirects.
 */
export default function Login() {
  const { login, isLoading } = useAuthStore();
  const { t } = useTranslation();

  return (
    <Box component="main" sx={{
      height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'linear-gradient(135deg, #1976d2 0%, #1565c0 45%, #0d3f74 100%)',
      p: 2,
    }}>
      <Paper elevation={0} sx={{
        p: { xs: 3, sm: 5 }, width: 400, borderRadius: 4, textAlign: 'center',
        boxShadow: '0 24px 60px rgba(0,0,0,0.35)',
      }}>
        <Box sx={{
          width: 52, height: 52, borderRadius: 2.5, mb: 2.5, mx: 'auto',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: 'linear-gradient(135deg, #1976d2, #1565c0)',
          boxShadow: '0 6px 16px rgba(25,118,210,0.4)',
        }}>
          <DashboardRoundedIcon sx={{ color: '#fff', fontSize: 28 }} />
        </Box>

        <Typography variant="h5" component="h1" fontWeight={800}>{t('auth.signIn')}</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3.5 }}>
          Sign in with your organization account to continue.
        </Typography>

        <Button
          variant="contained" fullWidth size="large"
          startIcon={isLoading ? undefined : <LoginRoundedIcon />}
          disabled={isLoading}
          onClick={() => void login()}
          sx={{
            py: 1.2, borderRadius: 2, textTransform: 'none', fontSize: '0.95rem', fontWeight: 700,
            background: 'linear-gradient(135deg, #1976d2, #1565c0)',
            boxShadow: '0 8px 20px rgba(25,118,210,0.35)',
            '&:hover': { background: 'linear-gradient(135deg, #1565c0, #0d3f74)' },
          }}
        >
          {isLoading ? <CircularProgress size={22} color="inherit" /> : t('auth.login')}
        </Button>
      </Paper>
    </Box>
  );
}
