import { useState } from 'react';
import {
  IconButton, Badge, Popover, Box, Typography, List,
  ListItemButton, ListItemText, Button, Divider, Chip,
} from '@mui/material';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { useNavigate } from 'react-router-dom';
import { useMyInbox, useUnreadCount, useMarkNotificationRead } from '../hooks/useNotifications';
import { ROUTES } from '@/core/router/routes';

export default function NotificationsBell() {
  const [anchor, setAnchor] = useState<null | HTMLElement>(null);
  const { items, isLoading } = useMyInbox({ page: 0, size: 5 });
  const { data: unreadCount } = useUnreadCount();
  const { mutate: markRead } = useMarkNotificationRead();
  const navigate = useNavigate();

  if (isLoading) {
    return (
      <IconButton color="inherit" disabled aria-label="Notifications">
        <Badge><NotificationsIcon /></Badge>
      </IconButton>
    );
  }

  return (
    <>
      <IconButton
        color="inherit"
        onClick={(e) => setAnchor(e.currentTarget)}
        aria-label={`Notifications${unreadCount ? `, ${unreadCount} unread` : ''}`}
        aria-haspopup="true"
        aria-expanded={Boolean(anchor)}
      >
        <Badge badgeContent={unreadCount ?? 0} color="error" max={9}>
          <NotificationsIcon />
        </Badge>
      </IconButton>

      <Popover
        open={Boolean(anchor)}
        anchorEl={anchor}
        onClose={() => setAnchor(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Box sx={{ width: 360 }}>
          <Box sx={{ p: 2 }}>
            <Typography variant="subtitle1" fontWeight={700}>
              Notifications
              {Boolean(unreadCount) && <Chip label={unreadCount} size="small" color="error" sx={{ ml: 1 }} />}
            </Typography>
          </Box>
          <Divider />
          <List disablePadding sx={{ maxHeight: 320, overflow: 'auto' }}>
            {items.length === 0 && (
              <Box sx={{ p: 3, textAlign: 'center' }}>
                <Typography variant="body2" color="text.secondary">No notifications</Typography>
              </Box>
            )}
            {items.map((n) => (
              <ListItemButton
                key={n.id}
                onClick={() => { if (!n.read) markRead(n.id!); }}
                sx={{ opacity: n.read ? 0.6 : 1, bgcolor: n.read ? 'transparent' : 'action.hover' }}
              >
                <ListItemText
                  primary={n.notification?.title ?? '(untitled)'}
                  secondary={
                    <>
                      <span>{n.notification?.body}</span><br />
                      <span style={{ fontSize: '0.7rem', opacity: 0.7 }}>
                        {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                      </span>
                    </>
                  }
                />
              </ListItemButton>
            ))}
          </List>
          <Divider />
          <Box sx={{ p: 1, textAlign: 'center' }}>
            <Button size="small" onClick={() => { navigate(ROUTES.NOTIFICATIONS); setAnchor(null); }}>
              View all notifications
            </Button>
          </Box>
        </Box>
      </Popover>
    </>
  );
}
