import { ErrorBoundary } from 'react-error-boundary';
import * as Sentry from '@sentry/react';
import { Box, Typography, Button } from '@mui/material';
import type { ReactNode } from 'react';

function Fallback({ error, resetErrorBoundary }: { error: unknown; resetErrorBoundary: () => void }) {
  const message = error instanceof Error ? error.message : String(error);
  return (
    <Box sx={{ p: 6, textAlign: 'center' }}>
      <Typography variant="h6" color="error" mb={1}>Something went wrong</Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>{message}</Typography>
      <Button variant="outlined" onClick={resetErrorBoundary}>Try again</Button>
    </Box>
  );
}

const SentryErrorBoundary = Sentry.withErrorBoundary(
  ({ children }: { children: ReactNode }) => <>{children}</>,
  { fallback: ({ error, resetError }) => <Fallback error={error} resetErrorBoundary={resetError} /> }
);

export default function PageErrorBoundary({ children }: { children: ReactNode }) {
  return (
    <ErrorBoundary FallbackComponent={Fallback}>
      <SentryErrorBoundary>{children}</SentryErrorBoundary>
    </ErrorBoundary>
  );
}
