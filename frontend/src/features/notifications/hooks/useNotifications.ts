import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, unwrapPage, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { STALE } from '@/shared/lib/queryClient';
import { useCurrentBackendUser } from '@/core/auth/useCurrentBackendUser';
import {
  NOTIFICATIONS_BASE_PATH, NOTIFICATIONS_KEY, USER_NOTIFICATIONS_BASE_PATH, USER_NOTIFICATIONS_KEY,
  type CreateNotificationRequest, type Notification, type UserNotification,
} from '../api/notificationsApi';

function notificationsResource() {
  return useCrudResource<Notification, CreateNotificationRequest, never>(NOTIFICATIONS_BASE_PATH, NOTIFICATIONS_KEY);
}

export function useNotificationsList(params: ListParams = {}) {
  return notificationsResource().useList(params);
}
export function useCreateNotification() {
  return notificationsResource().useCreate();
}

/**
 * The current user's own notification inbox — join of UserNotification rows
 * for their account. Keyed by the resolved *backend* user id, not the JWT
 * `sub` — those are different UUIDs (see useCurrentBackendUser).
 */
export function useMyUserNotifications(params: ListParams = {}) {
  const { data: backendUser } = useCurrentBackendUser();
  const userId = backendUser?.id;
  return useQuery({
    queryKey: [USER_NOTIFICATIONS_KEY, 'mine', userId, params],
    queryFn: () => apiClient.get<ApiResponse<UserNotification[]>>(USER_NOTIFICATIONS_BASE_PATH, { params: { ...params, userId } }).then(unwrapPage),
    enabled: Boolean(userId),
    staleTime: STALE.SHORT,
  });
}

export function useUnreadCount() {
  const { data: backendUser } = useCurrentBackendUser();
  const userId = backendUser?.id;
  return useQuery({
    queryKey: [USER_NOTIFICATIONS_KEY, 'unread-count', userId],
    queryFn: () => apiClient.get<ApiResponse<number>>(`${USER_NOTIFICATIONS_BASE_PATH}/unread-count`, { params: { userId } }).then(unwrap),
    enabled: Boolean(userId),
    staleTime: STALE.REALTIME,
  });
}

export function useMarkNotificationRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiClient.patch<ApiResponse<UserNotification>>(`${USER_NOTIFICATIONS_BASE_PATH}/${id}/read`).then(unwrap),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: [USER_NOTIFICATIONS_KEY] });
    },
  });
}

export interface InboxItem extends UserNotification {
  notification?: Notification;
}

/** Joins the current user's UserNotification rows with their Notification content for display. */
export function useMyInbox(params: ListParams = {}) {
  const userNotifications = useMyUserNotifications(params);
  const notificationIds = (userNotifications.data?.items ?? []).map((un) => un.notificationId).filter((id): id is string => Boolean(id));

  const notifications = useQuery({
    queryKey: [NOTIFICATIONS_KEY, 'by-ids', notificationIds],
    queryFn: async () => {
      const results = await Promise.all(notificationIds.map((id) => apiClient.get<ApiResponse<Notification>>(`${NOTIFICATIONS_BASE_PATH}/${id}`).then(unwrap)));
      return new Map(results.map((n) => [n.id, n]));
    },
    enabled: notificationIds.length > 0,
    staleTime: STALE.MEDIUM,
  });

  const items: InboxItem[] = (userNotifications.data?.items ?? []).map((un) => ({
    ...un,
    notification: notifications.data?.get(un.notificationId),
  }));

  return {
    items,
    pagination: userNotifications.data?.pagination,
    isLoading: userNotifications.isLoading || notifications.isLoading,
    isError: userNotifications.isError || notifications.isError,
    refetch: userNotifications.refetch,
  };
}
