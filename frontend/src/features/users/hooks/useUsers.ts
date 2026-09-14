import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { USERS_BASE_PATH, USERS_KEY, type CreateUserRequest, type UpdateUserRequest, type User } from '../api/usersApi';

function useResource() {
  return useCrudResource<User, CreateUserRequest, UpdateUserRequest>(USERS_BASE_PATH, USERS_KEY);
}

export function useUsers(params: ListParams) {
  return useResource().useList(params);
}

export function useUser(id: string | undefined) {
  return useResource().useOne(id);
}

export function useCreateUser() {
  return useResource().useCreate();
}

export function useUpdateUser() {
  return useResource().useUpdate();
}

export function useDeleteUser() {
  return useResource().useRemove();
}
