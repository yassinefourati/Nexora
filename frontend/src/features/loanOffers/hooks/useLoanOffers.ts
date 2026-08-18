import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import {
  LOAN_OFFERS_BASE_PATH,
  LOAN_OFFERS_KEY,
  type CreateLoanOfferRequest,
  type DeclineLoanOfferRequest,
  type LoanOffer,
} from '../api/loanOffersApi';

function resource() {
  return useCrudResource<LoanOffer, CreateLoanOfferRequest, never>(
    LOAN_OFFERS_BASE_PATH,
    LOAN_OFFERS_KEY,
  );
}

export function useLoanOffers(params: ListParams = {}) {
  return resource().useList(params);
}
export function useLoanOffer(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateLoanOffer() {
  return resource().useCreate();
}
export function useDeleteLoanOffer() {
  return resource().useRemove();
}

export function useAcceptLoanOffer() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.post<ApiResponse<LoanOffer>>(`${LOAN_OFFERS_BASE_PATH}/${id}/accept`).then(unwrap),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_OFFERS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_OFFERS_KEY, 'detail', id] });
      notify('Loan offer accepted', 'success');
    },
  });
}

export function useDeclineLoanOffer() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: DeclineLoanOfferRequest }) =>
      apiClient.post<ApiResponse<LoanOffer>>(`${LOAN_OFFERS_BASE_PATH}/${id}/decline`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [LOAN_OFFERS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [LOAN_OFFERS_KEY, 'detail', id] });
      notify('Loan offer declined', 'success');
    },
  });
}
