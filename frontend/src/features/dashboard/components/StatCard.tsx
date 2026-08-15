import { Paper, Typography, Box, Skeleton } from '@mui/material';
import type { ReactNode } from 'react';

interface StatCardProps {
  title: string;
  value: string | number;
  icon?: ReactNode;
  color?: string;
  loading?: boolean;
  trend?: { value: number; label: string };   // e.g. { value: 12, label: 'vs last month' }
}

export default function StatCard({ title, value, icon, color, loading, trend }: StatCardProps) {
  if (loading) {
    return (
      <Paper elevation={2} sx={{ p: 3, borderRadius: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
        <Skeleton variant="rounded" width={48} height={48} />
        <Box sx={{ flex: 1 }}>
          <Skeleton variant="text" width="60%" height={18} />
          <Skeleton variant="text" width="40%" height={36} />
        </Box>
      </Paper>
    );
  }

  const trendPositive = (trend?.value ?? 0) >= 0;

  return (
    <Paper
      elevation={2}
      sx={{
        p: 3, borderRadius: 3, display: 'flex', alignItems: 'center', gap: 2,
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': { transform: 'translateY(-3px)', boxShadow: 6 },
      }}
    >
      {icon && (
        <Box sx={{
          width: 48, height: 48, borderRadius: 2,
          bgcolor: color ?? 'primary.main',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: '#fff', flexShrink: 0,
          boxShadow: `0 4px 14px ${color ?? '#1976d2'}44`,
        }}>
          {icon}
        </Box>
      )}
      <Box sx={{ flex: 1, minWidth: 0 }}>
        <Typography variant="subtitle2" component="p" color="text.secondary" noWrap>{title}</Typography>
        <Typography variant="h5" component="p" fontWeight={700} noWrap>
          {typeof value === 'number' ? value.toLocaleString() : value}
        </Typography>
        {trend && (
          <Typography variant="caption" color={trendPositive ? 'success.main' : 'error.main'} fontWeight={600}>
            {trendPositive ? '↑' : '↓'} {Math.abs(trend.value)}% {trend.label}
          </Typography>
        )}
      </Box>
    </Paper>
  );
}
