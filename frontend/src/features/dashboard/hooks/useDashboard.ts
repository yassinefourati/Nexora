import { useQuery } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { STALE } from '@/shared/lib/queryClient';
import type { ApiResponse } from '@/core/api/envelope';

/**
 * Real backend has no /dashboard/* endpoints — there is no dashboard
 * controller at all. Every figure here is derived from the real list
 * endpoints' pagination.totalElements (a cheap, always-accurate count),
 * fetched with size=1 so the row payload itself is never used.
 */
function useResourceCount(path: string) {
  return useQuery({
    queryKey: ['dashboard', 'count', path],
    queryFn: () => apiClient.get<ApiResponse<unknown[]>>(path, { params: { page: 0, size: 1 } })
      .then((r) => r.data.pagination?.totalElements ?? 0),
    staleTime: STALE.SHORT,
  });
}

export function useUserCount() { return useResourceCount('/users'); }
export function useOrganizationCount() { return useResourceCount('/organizations'); }
export function useRoleCount() { return useResourceCount('/roles'); }

/** Loan portfolio counts — same cheap pagination.totalElements approach. Each is a total count; the list endpoints don't support server-side status filtering. */
export function useLoanApplicationCount() { return useResourceCount('/loan-applications'); }
export function useLoanAccountCount() { return useResourceCount('/loan-accounts'); }
export function useCollectionCaseCount() { return useResourceCount('/collection-cases'); }
export function useLoanDisbursementCount() { return useResourceCount('/loan-disbursements'); }
export function useActiveSessionCount() {
  return useQuery({
    queryKey: ['dashboard', 'active-sessions'],
    queryFn: () => apiClient.get<ApiResponse<unknown[]>>('/sessions', { params: { page: 0, size: 1 } })
      .then((r) => r.data.pagination?.totalElements ?? 0),
    staleTime: STALE.REALTIME,
  });
}
