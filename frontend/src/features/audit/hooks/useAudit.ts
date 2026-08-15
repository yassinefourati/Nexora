import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { AUDIT_LOGS_BASE_PATH, AUDIT_LOGS_KEY, type AuditLog } from '../api/auditApi';

function resource() {
  return useCrudResource<AuditLog, never, never>(AUDIT_LOGS_BASE_PATH, AUDIT_LOGS_KEY);
}

export function useAuditLog(params: ListParams = {}) {
  return resource().useList(params);
}
