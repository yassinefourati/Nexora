import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrap, type ApiResponse } from '@/core/api/envelope';
import { useCrudResource, type ListParams } from '@/core/api/useCrudResource';
import { STALE } from '@/shared/lib/queryClient';
import { useAppStore } from '@/shared/stores/useAppStore';
import {
  PERMISSIONS_BASE_PATH, PERMISSIONS_KEY, ROLE_PERMISSIONS_BASE_PATH, ROLE_PERMISSIONS_KEY,
  type CreatePermissionRequest, type CreateRolePermissionRequest, type Permission, type RolePermission, type UpdatePermissionRequest,
} from '../api/permissionsApi';

function permissionsResource() {
  return useCrudResource<Permission, CreatePermissionRequest, UpdatePermissionRequest>(PERMISSIONS_BASE_PATH, PERMISSIONS_KEY);
}

export function usePermissionsList(params: ListParams = {}) {
  return permissionsResource().useList(params);
}

export function useCreatePermission() {
  return permissionsResource().useCreate();
}

export function useUpdatePermission() {
  return permissionsResource().useUpdate();
}

export function useDeletePermission() {
  return permissionsResource().useRemove();
}

/** Permissions granted to a single role — not a top-level list endpoint, so no pagination block. */
export function useRolePermissions(roleId: string | undefined) {
  return useQuery({
    queryKey: [ROLE_PERMISSIONS_KEY, 'by-role', roleId],
    queryFn: () => apiClient.get<ApiResponse<RolePermission[]>>(`${ROLE_PERMISSIONS_BASE_PATH}/by-role/${roleId}`).then(unwrap),
    enabled: Boolean(roleId),
    staleTime: STALE.SHORT,
  });
}

export function useGrantRolePermission() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (body: CreateRolePermissionRequest) =>
      apiClient.post<ApiResponse<RolePermission>>(ROLE_PERMISSIONS_BASE_PATH, body).then(unwrap),
    onSuccess: (_data, body) => {
      void queryClient.invalidateQueries({ queryKey: [ROLE_PERMISSIONS_KEY, 'by-role', body.roleId] });
      notify('Permission granted', 'success');
    },
  });
}

export function useRevokeRolePermission() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: ({ id }: { id: string; roleId: string }) =>
      apiClient.delete<ApiResponse<void>>(`${ROLE_PERMISSIONS_BASE_PATH}/${id}`).then(unwrap),
    onSuccess: (_data, { roleId }) => {
      void queryClient.invalidateQueries({ queryKey: [ROLE_PERMISSIONS_KEY, 'by-role', roleId] });
      notify('Permission revoked', 'success');
    },
  });
}
