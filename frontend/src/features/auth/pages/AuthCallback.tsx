import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, CircularProgress, Typography, Alert } from '@mui/material';
import { useAuthStore } from '@/core/auth/stores/useAuthStore';
import { ROUTES } from '@/core/router/routes';

/** Landing page for the OIDC Authorization Code redirect back from Keycloak. */
export default function AuthCallback() {
  const handleCallback = useAuthStore((s) => s.handleCallback);
  const navigate = useNavigate();
  const ran = useRef(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (ran.current) return;
    ran.current = true;
    handleCallback()
      .then(() => navigate(ROUTES.HOME, { replace: true }))
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Sign-in failed'));
  }, [handleCallback, navigate]);

  return (
    <Box sx={{ height: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
      {error ? (
        <Alert severity="error" sx={{ maxWidth: 420 }}>{error}</Alert>
      ) : (
        <>
          <CircularProgress />
          <Typography variant="body2" color="text.secondary">Completing sign-in…</Typography>
        </>
      )}
    </Box>
  );
}
