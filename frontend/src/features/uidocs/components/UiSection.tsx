import { Box, Typography, Divider, Chip, Alert } from '@mui/material';
import type { ReactNode } from 'react';

interface Props {
  id: string;
  title: string;
  subtitle?: string;
  badge?: string;
  badgeColor?: 'primary' | 'success' | 'warning' | 'error' | 'default';
  note?: string;
  children: ReactNode;
}

export default function UiSection({ id, title, subtitle, badge, badgeColor = 'primary', note, children }: Props) {
  return (
    <Box id={id} sx={{ mb: 7, scrollMarginTop: 80 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 0.5 }}>
        <Typography variant="h5" fontWeight={700}>{title}</Typography>
        {badge && <Chip label={badge} size="small" color={badgeColor} />}
      </Box>
      {subtitle && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5, maxWidth: 700, lineHeight: 1.7 }}>
          {subtitle}
        </Typography>
      )}
      {note && <Alert severity="info" sx={{ mb: 2 }}>{note}</Alert>}
      <Divider sx={{ mb: 3 }} />
      {children}
    </Box>
  );
}
