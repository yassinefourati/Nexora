import { Box, Typography, Button } from '@mui/material';
import type { ReactNode } from 'react';

interface EmptyStateProps {
  icon: ReactNode;
  title: string;
  description?: string;
  action?: { label: string; onClick: () => void };
}

export default function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', py: 10, gap: 2, color: 'text.secondary' }}>
      <Box sx={{ fontSize: 64, opacity: 0.3, lineHeight: 1 }}>{icon}</Box>
      <Typography variant="h6" fontWeight={600} color="text.primary">{title}</Typography>
      {description && <Typography variant="body2" color="text.secondary" textAlign="center" maxWidth={360}>{description}</Typography>}
      {action && <Button variant="contained" onClick={action.onClick} sx={{ mt: 1 }}>{action.label}</Button>}
    </Box>
  );
}
