import { z } from 'zod';

export const EVENT_TYPE_OPTIONS = [
  'application_submitted',
  'kyc_completed',
  'underwriting_decided',
  'loan_approved',
  'loan_rejected',
  'offer_issued',
  'contract_finalized',
  'disbursement_completed',
  'payment_due',
  'payment_overdue',
] as const;

export const CHANNEL_OPTIONS = ['email', 'sms', 'in_app'] as const;

export const loanNotificationSchema = z.object({
  loanApplicationId: z.string().uuid(),
  eventType: z.string().min(1).max(50),
  title: z.string().min(1).max(255),
  body: z.string().min(1),
  channel: z.enum(CHANNEL_OPTIONS),
});
export type LoanNotificationFormData = z.infer<typeof loanNotificationSchema>;
