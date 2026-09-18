import type { components } from '@/shared/types/api.generated';

export type CollectionCase = components['schemas']['CollectionCaseResponse'];
export type CreateCollectionCaseRequest = components['schemas']['CreateCollectionCaseRequest'];
export type EscalateCollectionCaseRequest = components['schemas']['EscalateCollectionCaseRequest'];
export type ResolveCollectionCaseRequest = components['schemas']['ResolveCollectionCaseRequest'];
export type WriteOffCollectionCaseRequest = components['schemas']['WriteOffCollectionCaseRequest'];

export const COLLECTION_CASES_BASE_PATH = '/collection-cases';
export const COLLECTION_CASES_KEY = 'collectionCases';
