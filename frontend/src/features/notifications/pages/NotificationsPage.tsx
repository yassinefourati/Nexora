import { useState } from 'react';
import { Box, Typography, Paper, List, ListItem, ListItemText, Chip, Divider, Stack, Skeleton, Pagination } from '@mui/material';
import { useMyInbox, useMarkNotificationRead } from '../hooks/useNotifications';

function NotificationSkeleton() {
  return (
    <Stack spacing={0} divider={<Divider />}>
      {Array.from({ length: 6 }).map((_, i) => (
        <Box key={i} sx={{ px: 2, py: 2, opacity: 1 - i * 0.1 }}>
          <Skeleton variant="text" width={160} height={20} />
          <Skeleton variant="text" width="80%" height={16} />
          <Skeleton variant="text" width={100} height={14} />
        </Box>
      ))}
    </Stack>
  );
}

export default function NotificationsPage() {
  const [page, setPage] = useState(0);
  const { items, pagination, isLoading } = useMyInbox({ page, size: 20 });
  const { mutate: markRead } = useMarkNotificationRead();

  return (
    <Box>
      <Typography variant="h4" component="h1" fontWeight={700} mb={3}>Notifications</Typography>
      <Paper elevation={2} sx={{ borderRadius: 3 }}>
        {isLoading ? (
          <NotificationSkeleton />
        ) : items.length === 0 ? (
          <Box sx={{ p: 6, textAlign: 'center' }}>
            <Typography color="text.secondary">No notifications yet.</Typography>
          </Box>
        ) : (
          <List disablePadding>
            {items.map((n, i) => (
              <ListItem
                key={n.id}
                divider={i < items.length - 1}
                sx={{ py: 2, bgcolor: n.read ? 'transparent' : 'action.hover', cursor: n.read ? 'default' : 'pointer' }}
                onClick={() => { if (!n.read) markRead(n.id!); }}
              >
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                      <Typography variant="subtitle2" component="span">{n.notification?.title ?? '(untitled)'}</Typography>
                      {n.notification?.channel && <Chip label={n.notification.channel} size="small" variant="outlined" />}
                      {!n.read && <Chip label="New" size="small" color="primary" />}
                    </Box>
                  }
                  secondary={
                    <>
                      <span>{n.notification?.body}</span>
                      <br />
                      <Typography variant="caption" color="text.secondary">
                        {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                      </Typography>
                    </>
                  }
                />
              </ListItem>
            ))}
          </List>
        )}
      </Paper>
      {pagination && pagination.totalPages > 1 && (
        <Stack alignItems="center" mt={2}>
          <Pagination count={pagination.totalPages} page={page + 1} onChange={(_, p) => setPage(p - 1)} />
        </Stack>
      )}
    </Box>
  );
}
