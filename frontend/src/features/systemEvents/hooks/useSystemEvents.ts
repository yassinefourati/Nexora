import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { SYSTEM_EVENTS_BASE_PATH, SYSTEM_EVENTS_KEY, type SystemEvent } from '../api/systemEventsApi';

function resource() {
  return useCrudResource<SystemEvent, never, never>(SYSTEM_EVENTS_BASE_PATH, SYSTEM_EVENTS_KEY);
}

export function useSystemEvents(params: ListParams = {}) {
  return resource().useList(params);
}
