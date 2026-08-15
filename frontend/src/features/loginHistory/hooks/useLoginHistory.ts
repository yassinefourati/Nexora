import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { LOGIN_HISTORY_BASE_PATH, LOGIN_HISTORY_KEY, type LoginHistory } from '../api/loginHistoryApi';

function resource() {
  return useCrudResource<LoginHistory, never, never>(LOGIN_HISTORY_BASE_PATH, LOGIN_HISTORY_KEY);
}

export function useLoginHistory(params: ListParams = {}) {
  return resource().useList(params);
}
