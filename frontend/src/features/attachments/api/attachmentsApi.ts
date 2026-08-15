import type { components } from '@/shared/types/api.generated';

export type Attachment = components['schemas']['AttachmentResponse'];
export type CreateAttachmentRequest = components['schemas']['CreateAttachmentRequest'];

export const ATTACHMENTS_BASE_PATH = '/attachments';
export const ATTACHMENTS_KEY = 'attachments';
