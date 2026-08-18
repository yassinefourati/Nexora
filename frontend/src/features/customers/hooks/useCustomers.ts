import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { CUSTOMERS_BASE_PATH, CUSTOMERS_KEY, type CreateCustomerRequest, type UpdateCustomerRequest, type Customer } from '../api/customersApi';

function resource() {
  return useCrudResource<Customer, CreateCustomerRequest, UpdateCustomerRequest>(CUSTOMERS_BASE_PATH, CUSTOMERS_KEY);
}

export function useCustomers(params: ListParams = {}) {
  return resource().useList(params);
}
export function useCustomer(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateCustomer() {
  return resource().useCreate();
}
export function useUpdateCustomer() {
  return resource().useUpdate();
}
export function useDeleteCustomer() {
  return resource().useRemove();
}
