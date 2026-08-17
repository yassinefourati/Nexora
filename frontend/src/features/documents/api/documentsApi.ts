import type { components } from '@/shared/types/api.generated';

export type Document = components['schemas']['DocumentResponse'];
export type CreateDocumentRequest = components['schemas']['CreateDocumentRequest'];

export const DOCUMENTS_BASE_PATH = '/documents';
export const DOCUMENTS_KEY = 'documents';
