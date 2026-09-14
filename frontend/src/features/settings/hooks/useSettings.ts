import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { SETTINGS_BASE_PATH, SETTINGS_KEY, type CreateSettingRequest, type UpdateSettingRequest, type Setting } from '../api/settingsApi';

function useResource() {
  return useCrudResource<Setting, CreateSettingRequest, UpdateSettingRequest>(SETTINGS_BASE_PATH, SETTINGS_KEY);
}

export function useSettings(params: ListParams = {}) {
  return useResource().useList(params);
}
export function useCreateSetting() {
  return useResource().useCreate();
}
export function useUpdateSetting() {
  return useResource().useUpdate();
}
export function useDeleteSetting() {
  return useResource().useRemove();
}
