import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { SETTINGS_BASE_PATH, SETTINGS_KEY, type CreateSettingRequest, type UpdateSettingRequest, type Setting } from '../api/settingsApi';

function resource() {
  return useCrudResource<Setting, CreateSettingRequest, UpdateSettingRequest>(SETTINGS_BASE_PATH, SETTINGS_KEY);
}

export function useSettings(params: ListParams = {}) {
  return resource().useList(params);
}
export function useCreateSetting() {
  return resource().useCreate();
}
export function useUpdateSetting() {
  return resource().useUpdate();
}
export function useDeleteSetting() {
  return resource().useRemove();
}
