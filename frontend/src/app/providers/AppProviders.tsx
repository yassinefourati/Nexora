import { BrowserRouter } from 'react-router-dom';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { useEffect, useState, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { queryClient } from '@/shared/lib/queryClient';
import { useUIStore } from '@/shared/stores/useUIStore';
import { useAuthStore } from '@/core/auth/stores/useAuthStore';
import { getTheme } from '@/core/theme';
import GlobalFeedback from '@/shared/components/GlobalFeedback';
import ConfirmDialog from '@/shared/components/ConfirmDialog';
import SessionTimeoutDialog from '@/shared/components/SessionTimeoutDialog';
import { useOfflineQueue } from '@/shared/hooks/useOfflineQueue';
import { useLoadingBar } from '@/shared/hooks/useLoadingBar';
import { useIdleTimer } from '@/shared/hooks/useIdleTimer';
import NotificationsSocketWatcher from '@/shared/components/NotificationsSocketWatcher';

const IDLE_TIMEOUT = 15 * 60 * 1000;
const WARNING_TIME = 60 * 1000;

function OfflineWatcher()  { 
  useOfflineQueue();        
  return null; 
}

function LoadingBarWatcher() { 
  useLoadingBar();          
  return null; 
}

function IdleWatcher() {
  const [showWarning, setShowWarning] = useState(false);
  const { logout, isAuthenticated } = useAuthStore();
  useIdleTimer({
    timeout:     IDLE_TIMEOUT,
    warningTime: WARNING_TIME,
    onWarning: () => { 
      if (isAuthenticated) setShowWarning(true); 
    },
    onIdle: () => { 
      setShowWarning(false); 
      if (isAuthenticated) 
        logout(); 
    },
  });
  return (
    <SessionTimeoutDialog
      open={showWarning}
      warningSeconds={60}
      onStayLoggedIn={() => setShowWarning(false)}
      onLogout={() => { setShowWarning(false); logout(); }}
    />
  );
}

export default function AppProviders({ children }: { children: ReactNode }) {
  const { theme, language } = useUIStore();
  const { i18n } = useTranslation();
  useEffect(() => { if (i18n.language !== language) i18n.changeLanguage(language); }, [language, i18n]);

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={getTheme(theme)}>
        <CssBaseline />
        <GlobalFeedback />
        <ConfirmDialog />
        <IdleWatcher />
        <BrowserRouter>
          <OfflineWatcher />
          <LoadingBarWatcher />
          <NotificationsSocketWatcher />
          {children}
        </BrowserRouter>
      </ThemeProvider>
      {new URLSearchParams(window.location.search).get('devtools') === '1' && (
        <ReactQueryDevtools initialIsOpen />
      )}
    </QueryClientProvider>
  );
}