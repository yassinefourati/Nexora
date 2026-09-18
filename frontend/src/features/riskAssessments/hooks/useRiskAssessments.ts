import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { useAppStore } from '@/shared/stores/useAppStore';
import { RISK_ASSESSMENTS_BASE_PATH, RISK_ASSESSMENTS_KEY, type CreateRiskAssessmentRequest, type RiskAssessment } from '../api/riskAssessmentsApi';

function resource() {
  return useCrudResource<RiskAssessment, CreateRiskAssessmentRequest, never>(RISK_ASSESSMENTS_BASE_PATH, RISK_ASSESSMENTS_KEY);
}

export function useRiskAssessments(params: ListParams = {}) {
  return resource().useList(params);
}
export function useRiskAssessment(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateRiskAssessment() {
  return resource().useCreate();
}

export function useProcessRiskAssessment() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) =>
      apiClient.post<ApiResponse<RiskAssessment>>(`${RISK_ASSESSMENTS_BASE_PATH}/${id}/process`).then(unwrap),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: [RISK_ASSESSMENTS_KEY, 'list'] });
      void queryClient.invalidateQueries({ queryKey: [RISK_ASSESSMENTS_KEY, 'detail', id] });
      notify('Risk assessment processed', 'success');
    },
  });
}
