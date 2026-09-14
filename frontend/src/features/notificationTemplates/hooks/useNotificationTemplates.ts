import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import {
  NOTIFICATION_TEMPLATES_BASE_PATH, NOTIFICATION_TEMPLATES_KEY,
  type CreateNotificationTemplateRequest, type UpdateNotificationTemplateRequest, type NotificationTemplate,
} from '../api/notificationTemplatesApi';

function useResource() {
  return useCrudResource<NotificationTemplate, CreateNotificationTemplateRequest, UpdateNotificationTemplateRequest>(NOTIFICATION_TEMPLATES_BASE_PATH, NOTIFICATION_TEMPLATES_KEY);
}

export function useNotificationTemplates(params: ListParams = {}) {
  return useResource().useList(params);
}
export function useCreateNotificationTemplate() {
  return useResource().useCreate();
}
export function useUpdateNotificationTemplate() {
  return useResource().useUpdate();
}
export function useDeleteNotificationTemplate() {
  return useResource().useRemove();
}
