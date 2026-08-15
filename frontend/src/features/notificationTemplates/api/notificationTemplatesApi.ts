import type { components } from '@/shared/types/api.generated';

export type NotificationTemplate = components['schemas']['NotificationTemplateResponse'];
export type CreateNotificationTemplateRequest = components['schemas']['CreateNotificationTemplateRequest'];
export type UpdateNotificationTemplateRequest = components['schemas']['UpdateNotificationTemplateRequest'];

export const NOTIFICATION_TEMPLATES_BASE_PATH = '/notification-templates';
export const NOTIFICATION_TEMPLATES_KEY = 'notification-templates';
