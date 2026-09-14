import { ErrorBoundary } from 'react-error-boundary';
import { useTranslation } from 'react-i18next';
import { Box, Typography, Button } from '@mui/material';
import type { ReactNode } from 'react';

function SectionFallback({ error, resetErrorBoundary }: { error: unknown; resetErrorBoundary: () => void }) {
  const { t } = useTranslation();
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
        {message || t('ui.sectionFailed')}
      </Typography>
      <Button size="small" variant="outlined" color="error" onClick={resetErrorBoundary}>
        {t('ui.retry')}
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
