import type { components } from '@/shared/types/api.generated';

export type LoanNotification = components['schemas']['LoanNotificationResponse'];
export type CreateLoanNotificationRequest = components['schemas']['CreateLoanNotificationRequest'];

export const LOAN_NOTIFICATIONS_BASE_PATH = '/loan-notifications';
export const LOAN_NOTIFICATIONS_KEY = 'loanNotifications';
