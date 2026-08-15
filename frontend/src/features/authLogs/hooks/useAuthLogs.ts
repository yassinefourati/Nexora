import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { AUTH_LOGS_BASE_PATH, AUTH_LOGS_KEY, type AuthLog } from '../api/authLogsApi';

function resource() {
  return useCrudResource<AuthLog, never, never>(AUTH_LOGS_BASE_PATH, AUTH_LOGS_KEY);
}

export function useAuthLogs(params: ListParams = {}) {
  return resource().useList(params);
}
