import type { components } from '@/shared/types/api.generated';

export type LoanOffer = components['schemas']['LoanOfferResponse'];
export type CreateLoanOfferRequest = components['schemas']['CreateLoanOfferRequest'];
export type DeclineLoanOfferRequest = components['schemas']['DeclineLoanOfferRequest'];

export const LOAN_OFFERS_BASE_PATH = '/loan-offers';
export const LOAN_OFFERS_KEY = 'loanOffers';
