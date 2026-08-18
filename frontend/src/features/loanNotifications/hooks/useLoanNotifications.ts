import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import {
  LOAN_NOTIFICATIONS_BASE_PATH,
  LOAN_NOTIFICATIONS_KEY,
  type CreateLoanNotificationRequest,
  type LoanNotification,
} from '../api/loanNotificationsApi';

function resource() {
  return useCrudResource<LoanNotification, CreateLoanNotificationRequest, never>(
    LOAN_NOTIFICATIONS_BASE_PATH,
    LOAN_NOTIFICATIONS_KEY,
  );
}

export function useLoanNotifications(params: ListParams = {}) {
  return resource().useList(params);
}
export function useLoanNotification(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateLoanNotification() {
  return resource().useCreate();
}
