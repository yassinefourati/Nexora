import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import type { StompSubscription } from '@stomp/stompjs';
import { useAuthStore } from '@/core/auth/stores/useAuthStore';
import { useWebSocketStore } from '@/shared/stores/useWebSocketStore';
import { USER_NOTIFICATIONS_KEY } from '@/features/notifications/api/notificationsApi';

/**
 * Connects the STOMP/SockJS client once authenticated and subscribes to the
 * current user's notification queue, invalidating the notifications queries
 * on push so the bell/inbox update live instead of only on next mount. This
 * is additive to the existing REST-fetch model — a client that misses a push
 * (offline, reconnecting, or simply not subscribed yet) still sees the
 * notification on its next regular fetch, so there's no correctness gap if
 * the socket never connects.
 */
export default function NotificationsSocketWatcher() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const accessToken = useAuthStore((s) => s.accessToken);
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!isAuthenticated || !accessToken) {
      useWebSocketStore.getState().disconnect();
      return;
    }

    useWebSocketStore.getState().connect(accessToken);

    let subscription: StompSubscription | null = null;
    const unsubscribeFromStore = useWebSocketStore.subscribe((state) => {
      if (state.connected && !subscription) {
        subscription = state.subscribe('/user/queue/notifications', () => {
          void queryClient.invalidateQueries({ queryKey: [USER_NOTIFICATIONS_KEY] });
        });
      }
    });

    return () => {
      unsubscribeFromStore();
      subscription?.unsubscribe();
    };
  }, [isAuthenticated, accessToken, queryClient]);

  return null;
}
