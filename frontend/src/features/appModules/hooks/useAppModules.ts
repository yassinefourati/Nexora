import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { APP_MODULES_BASE_PATH, APP_MODULES_KEY, type CreateAppModuleRequest, type UpdateAppModuleRequest, type AppModule } from '../api/appModulesApi';

function resource() {
  return useCrudResource<AppModule, CreateAppModuleRequest, UpdateAppModuleRequest>(APP_MODULES_BASE_PATH, APP_MODULES_KEY);
}

export function useAppModules(params: ListParams = {}) {
  return resource().useList(params);
}
export function useCreateAppModule() {
  return resource().useCreate();
}
export function useUpdateAppModule() {
  return resource().useUpdate();
}
export function useDeleteAppModule() {
  return resource().useRemove();
}
