import type { components } from '@/shared/types/api.generated';

export type Setting = components['schemas']['SettingResponse'];
export type CreateSettingRequest = components['schemas']['CreateSettingRequest'];
export type UpdateSettingRequest = components['schemas']['UpdateSettingRequest'];

export const SETTINGS_BASE_PATH = '/settings';
export const SETTINGS_KEY = 'settings';

export const SETTING_SCOPES = ['global', 'organization'] as const;
