import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { USERS_BASE_PATH, USERS_KEY, type CreateUserRequest, type UpdateUserRequest, type User } from '../api/usersApi';

function resource() {
  return useCrudResource<User, CreateUserRequest, UpdateUserRequest>(USERS_BASE_PATH, USERS_KEY);
}

export function useUsers(params: ListParams) {
  return resource().useList(params);
}

export function useUser(id: string | undefined) {
  return resource().useOne(id);
}

export function useCreateUser() {
  return resource().useCreate();
}

export function useUpdateUser() {
  return resource().useUpdate();
}

export function useDeleteUser() {
  return resource().useRemove();
}
