import type { components } from '@/shared/types/api.generated';

export type AppModule = components['schemas']['AppModuleResponse'];
export type CreateAppModuleRequest = components['schemas']['CreateAppModuleRequest'];
export type UpdateAppModuleRequest = components['schemas']['UpdateAppModuleRequest'];

export const APP_MODULES_BASE_PATH = '/app-modules';
export const APP_MODULES_KEY = 'app-modules';
