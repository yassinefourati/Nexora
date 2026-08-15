import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { ErrorBoundary } from 'react-error-boundary';
import '@/core/i18n';
import './index.css';
import App from './App';
import { initSentry } from '@/core/config/sentry';
import RootFallback from '@/shared/components/RootFallback';

initSentry();

const root = document.getElementById('root');
if (!root) throw new Error('Root element #root not found');
createRoot(root).render(
  <StrictMode>
    <ErrorBoundary FallbackComponent={RootFallback}>
      <App />
    </ErrorBoundary>
  </StrictMode>
);
