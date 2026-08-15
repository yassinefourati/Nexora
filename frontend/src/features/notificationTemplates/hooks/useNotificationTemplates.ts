import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import {
  NOTIFICATION_TEMPLATES_BASE_PATH, NOTIFICATION_TEMPLATES_KEY,
  type CreateNotificationTemplateRequest, type UpdateNotificationTemplateRequest, type NotificationTemplate,
} from '../api/notificationTemplatesApi';

function resource() {
  return useCrudResource<NotificationTemplate, CreateNotificationTemplateRequest, UpdateNotificationTemplateRequest>(NOTIFICATION_TEMPLATES_BASE_PATH, NOTIFICATION_TEMPLATES_KEY);
}

export function useNotificationTemplates(params: ListParams = {}) {
  return resource().useList(params);
}
export function useCreateNotificationTemplate() {
  return resource().useCreate();
}
export function useUpdateNotificationTemplate() {
  return resource().useUpdate();
}
export function useDeleteNotificationTemplate() {
  return resource().useRemove();
}
