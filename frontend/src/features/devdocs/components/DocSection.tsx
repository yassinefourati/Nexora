import { Box, Typography, Divider, Chip } from '@mui/material';
import type { ReactNode } from 'react';

interface Props {
  id: string;
  title: string;
  badge?: string;
  badgeColor?: 'primary' | 'success' | 'warning' | 'error' | 'default';
  description?: string;
  children: ReactNode;
}

export default function DocSection({ id, title, badge, badgeColor = 'primary', description, children }: Props) {
  return (
    <Box id={id} sx={{ mb: 6, scrollMarginTop: 80 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 0.75 }}>
        <Typography variant="h5" fontWeight={700}>{title}</Typography>
        {badge && <Chip label={badge} size="small" color={badgeColor} />}
      </Box>
      {description && <Typography variant="body2" color="text.secondary" mb={2} sx={{ maxWidth: 680, lineHeight: 1.7 }}>{description}</Typography>}
      <Divider sx={{ mb: 3 }} />
      {children}
    </Box>
  );
}
