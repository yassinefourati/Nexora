import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { LOAN_PRODUCTS_BASE_PATH, LOAN_PRODUCTS_KEY, type CreateLoanProductRequest, type UpdateLoanProductRequest, type LoanProduct } from '../api/loanProductsApi';

function resource() {
  return useCrudResource<LoanProduct, CreateLoanProductRequest, UpdateLoanProductRequest>(LOAN_PRODUCTS_BASE_PATH, LOAN_PRODUCTS_KEY);
}

export function useLoanProducts(params: ListParams = {}) {
  return resource().useList(params);
}
export function useLoanProduct(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateLoanProduct() {
  return resource().useCreate();
}
export function useUpdateLoanProduct() {
  return resource().useUpdate();
}
export function useDeleteLoanProduct() {
  return resource().useRemove();
}
