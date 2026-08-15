import type { AxiosResponse } from 'axios';

/**
 * Real backend response envelope (platform's ApiResponse<T>), exactly as
 * returned by GlobalExceptionHandler / every controller — not the fictional
 * {status, code, meta} shape the old mock API used.
 */
export interface ApiPagination {
  page: number;          // zero-based
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiError {
  code: string;
  details?: unknown;
}

export interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  timestamp: string;
  correlationId: string;
  data: T;
  pagination?: ApiPagination;
  error?: ApiError;
}

/** Unwraps the envelope's `data` field from an axios response. */
export function unwrap<T>(res: AxiosResponse<ApiResponse<T>>): T {
  return res.data.data;
}

/** Unwraps `data` + `pagination` together, for paged list endpoints. */
export function unwrapPage<T>(res: AxiosResponse<ApiResponse<T[]>>): { items: T[]; pagination: ApiPagination } {
  const { data, pagination } = res.data;
  if (!pagination) throw new Error('[API] Expected a paginated response but no pagination block was present.');
  return { items: data, pagination };
}

/** Backend page/size query params — zero-based page index, matches Spring Data Pageable. */
export interface PageParams {
  page?: number;   // zero-based
  size?: number;
  sort?: string;   // e.g. "createdAt,desc"
}
