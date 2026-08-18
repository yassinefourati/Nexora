import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import {
  CONTRACT_SIGNATURES_BASE_PATH,
  CONTRACT_SIGNATURES_KEY,
  type CreateContractSignatureRequest,
  type DeclineContractSignatureRequest,
  type ContractSignature,
} from '../api/contractSignaturesApi';

function resource() {
  return useCrudResource<ContractSignature, CreateContractSignatureRequest, never>(
    CONTRACT_SIGNATURES_BASE_PATH,
    CONTRACT_SIGNATURES_KEY,
  );
}

export function useContractSignatures(params: ListParams = {}) {
  return resource().useList(params);
}
export function useContractSignature(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateContractSignature() {
  return resource().useCreate();
}

export function useSignContractSignature() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.post<ApiResponse<ContractSignature>>(`${CONTRACT_SIGNATURES_BASE_PATH}/${id}/sign`).then(unwrap),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: [CONTRACT_SIGNATURES_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [CONTRACT_SIGNATURES_KEY, 'detail', id] });
      notify('Signature recorded', 'success');
    },
  });
}

export function useDeclineContractSignature() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: DeclineContractSignatureRequest }) =>
      apiClient.post<ApiResponse<ContractSignature>>(`${CONTRACT_SIGNATURES_BASE_PATH}/${id}/decline`, body).then(unwrap),
    onSuccess: (_data, { id }) => {
      void queryClient.invalidateQueries({ queryKey: [CONTRACT_SIGNATURES_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [CONTRACT_SIGNATURES_KEY, 'detail', id] });
      notify('Signature request declined', 'success');
    },
  });
}
