import type { components } from '@/shared/types/api.generated';

export type Comment = components['schemas']['CommentResponse'];
export type CreateCommentRequest = components['schemas']['CreateCommentRequest'];
export type UpdateCommentRequest = components['schemas']['UpdateCommentRequest'];

export const COMMENTS_BASE_PATH = '/comments';
export const COMMENTS_KEY = 'comments';
