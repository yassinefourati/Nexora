import type { components } from '@/shared/types/api.generated';

export type Notification = components['schemas']['NotificationResponse'];
export type CreateNotificationRequest = components['schemas']['CreateNotificationRequest'];
export type UserNotification = components['schemas']['UserNotificationResponse'];

export const NOTIFICATIONS_BASE_PATH = '/notifications';
export const NOTIFICATIONS_KEY = 'notifications';
export const USER_NOTIFICATIONS_BASE_PATH = '/user-notifications';
export const USER_NOTIFICATIONS_KEY = 'user-notifications';
