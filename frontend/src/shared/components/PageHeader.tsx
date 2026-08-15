import { Box, Typography, Stack } from '@mui/material';
import type { ReactNode } from 'react';
interface PageHeaderProps { title: string; description?: string; actions?: ReactNode; titleAdornment?: ReactNode; }
export default function PageHeader({ title, description, actions, titleAdornment }: PageHeaderProps) {
  return (
    <Box sx={{ mb: 3 }}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ xs: 'stretch', sm: 'flex-start' }} justifyContent="space-between">
        <Box sx={{ minWidth: 0 }}>
          <Stack direction="row" spacing={1.5} alignItems="center">
            <Typography variant="h4" component="h1" fontWeight={700} noWrap>{title}</Typography>
            {titleAdornment}
          </Stack>
          {description && <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, maxWidth: 640 }}>{description}</Typography>}
        </Box>
        {actions && <Stack direction="row" spacing={1} sx={{ flexShrink: 0, width: { xs: '100%', sm: 'auto' } }}>{actions}</Stack>}
      </Stack>
    </Box>
  );
}
