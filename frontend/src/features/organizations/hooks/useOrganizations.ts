import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { ORGANIZATIONS_BASE_PATH, ORGANIZATIONS_KEY, type CreateOrganizationRequest, type UpdateOrganizationRequest, type Organization } from '../api/organizationsApi';

function resource() {
  return useCrudResource<Organization, CreateOrganizationRequest, UpdateOrganizationRequest>(ORGANIZATIONS_BASE_PATH, ORGANIZATIONS_KEY);
}

export function useOrganizations(params: ListParams = {}) {
  return resource().useList(params);
}
export function useOrganization(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateOrganization() {
  return resource().useCreate();
}
export function useUpdateOrganization() {
  return resource().useUpdate();
}
export function useDeleteOrganization() {
  return resource().useRemove();
}
