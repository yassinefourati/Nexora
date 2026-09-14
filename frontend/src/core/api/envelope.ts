import type { AxiosResponse } from 'axios';

/**
 * Backend response contract.
 *
 * Successful (2xx) responses are wrapped in the platform's ApiResponse<T>
 * envelope. Errors are NOT wrapped: every 4xx/5xx is an RFC 9457 problem
 * details document (`application/problem+json`, ADR 0005) with the stable
 * `code` the UI branches on.
 */
export interface ApiPagination {
  page: number;          // zero-based
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiResponse<T> {
  success: true;
  status: number;
  message: string;
  timestamp: string;
  correlationId: string;
  data: T;
  pagination?: ApiPagination;
}

/** One field-level constraint violation, present on VALIDATION_FAILED and TYPE_MISMATCH. */
export interface Violation {
  field: string | null;
  code: string;
  message: string;
  rejectedValue?: unknown;
}

/** RFC 9457 problem details as produced by the backend for every error. */
export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail?: string;
  instance?: string;
  /** Stable UPPER_SNAKE_CASE code from the backend's ErrorCode catalog. */
  code: string;
  retryable: boolean;
  timestamp: string;
  correlationId?: string;
  violations?: Violation[];
}

export function isProblemDetail(body: unknown): body is ProblemDetail {
  return typeof body === 'object' && body !== null
    && typeof (body as ProblemDetail).status === 'number'
    && typeof (body as ProblemDetail).code === 'string';
}

/** Extracts the problem details from a rejected axios call, if the backend produced one. */
export function problemOf(error: unknown): ProblemDetail | undefined {
  const body = (error as { response?: { data?: unknown } } | undefined)?.response?.data;
  return isProblemDetail(body) ? body : undefined;
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
