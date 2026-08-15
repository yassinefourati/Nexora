import { ErrorBoundary } from 'react-error-boundary';
import { Box, Typography, Button } from '@mui/material';
import type { ReactNode } from 'react';

function SectionFallback({ error, resetErrorBoundary }: { error: unknown; resetErrorBoundary: () => void }) {
  const message = error instanceof Error ? error.message : String(error);
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 1,
        p: 3,
        border: '1px dashed',
        borderColor: 'error.light',
        borderRadius: 2,
        minHeight: 120,
      }}
    >
      <Typography variant="body2" color="error">
        {message || 'This section failed to load'}
      </Typography>
      <Button size="small" variant="outlined" color="error" onClick={resetErrorBoundary}>
        Retry
      </Button>
    </Box>
  );
}

/**
 * Wraps an individual section (chart, table, panel) so a crash doesn't take
 * down the whole page. Use this around any async-driven UI block.
 */
export default function SectionErrorBoundary({ children }: { children: ReactNode }) {
  return (
    <ErrorBoundary FallbackComponent={SectionFallback}>{children}</ErrorBoundary>
  );
}
