import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { DEPARTMENTS_BASE_PATH, DEPARTMENTS_KEY, type CreateDepartmentRequest, type UpdateDepartmentRequest, type Department } from '../api/departmentsApi';

function useResource() {
  return useCrudResource<Department, CreateDepartmentRequest, UpdateDepartmentRequest>(DEPARTMENTS_BASE_PATH, DEPARTMENTS_KEY);
}

export function useDepartments(params: ListParams = {}) {
  return useResource().useList(params);
}
export function useDepartment(id: string | undefined) {
  return useResource().useOne(id);
}
export function useCreateDepartment() {
  return useResource().useCreate();
}
export function useUpdateDepartment() {
  return useResource().useUpdate();
}
export function useDeleteDepartment() {
  return useResource().useRemove();
}
