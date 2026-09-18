import type { components } from '@/shared/types/api.generated';

export type RiskAssessment = components['schemas']['RiskAssessmentResponse'];
export type CreateRiskAssessmentRequest = components['schemas']['CreateRiskAssessmentRequest'];

export const RISK_ASSESSMENTS_BASE_PATH = '/risk-assessments';
export const RISK_ASSESSMENTS_KEY = 'riskAssessments';
