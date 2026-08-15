import type { components } from '@/shared/types/api.generated';

export type AuditLog = components['schemas']['AuditLogResponse'];

export const AUDIT_LOGS_BASE_PATH = '/audit-logs';
export const AUDIT_LOGS_KEY = 'audit-logs';
