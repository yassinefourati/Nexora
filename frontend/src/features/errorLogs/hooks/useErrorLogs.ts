import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { ERROR_LOGS_BASE_PATH, ERROR_LOGS_KEY, type ErrorLog } from '../api/errorLogsApi';

function resource() {
  return useCrudResource<ErrorLog, never, never>(ERROR_LOGS_BASE_PATH, ERROR_LOGS_KEY);
}

export function useErrorLogs(params: ListParams = {}) {
  return resource().useList(params);
}
