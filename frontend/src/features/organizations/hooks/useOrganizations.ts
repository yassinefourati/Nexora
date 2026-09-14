import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { ORGANIZATIONS_BASE_PATH, ORGANIZATIONS_KEY, type CreateOrganizationRequest, type UpdateOrganizationRequest, type Organization } from '../api/organizationsApi';

function useResource() {
  return useCrudResource<Organization, CreateOrganizationRequest, UpdateOrganizationRequest>(ORGANIZATIONS_BASE_PATH, ORGANIZATIONS_KEY);
}

export function useOrganizations(params: ListParams = {}) {
  return useResource().useList(params);
}
export function useOrganization(id: string | undefined) {
  return useResource().useOne(id);
}
export function useCreateOrganization() {
  return useResource().useCreate();
}
export function useUpdateOrganization() {
  return useResource().useUpdate();
}
export function useDeleteOrganization() {
  return useResource().useRemove();
}
