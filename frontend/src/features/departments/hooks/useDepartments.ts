import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { DEPARTMENTS_BASE_PATH, DEPARTMENTS_KEY, type CreateDepartmentRequest, type UpdateDepartmentRequest, type Department } from '../api/departmentsApi';

function resource() {
  return useCrudResource<Department, CreateDepartmentRequest, UpdateDepartmentRequest>(DEPARTMENTS_BASE_PATH, DEPARTMENTS_KEY);
}

export function useDepartments(params: ListParams = {}) {
  return resource().useList(params);
}
export function useDepartment(id: string | undefined) {
  return resource().useOne(id);
}
export function useCreateDepartment() {
  return resource().useCreate();
}
export function useUpdateDepartment() {
  return resource().useUpdate();
}
export function useDeleteDepartment() {
  return resource().useRemove();
}
