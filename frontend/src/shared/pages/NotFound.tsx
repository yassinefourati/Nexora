import { Box, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ROUTES } from '@/core/router/routes';
export default function NotFound() {
  const navigate = useNavigate();
  const { t } = useTranslation();
  return (
    <Box component="main" sx={{ height: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
      <Typography variant="h1" fontWeight={700} color="text.secondary">404</Typography>
      <Typography variant="h6" color="text.secondary">{t('common.pageNotFound')}</Typography>
      <Button variant="contained" onClick={() => navigate(ROUTES.HOME)}>{t('common.goHome')}</Button>
    </Box>
  );
}
