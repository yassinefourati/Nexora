import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/core/api/client';
import { unwrapPage, type ApiResponse } from '@/core/api/envelope';
import { useAppStore } from '@/shared/stores/useAppStore';
import { STALE } from '@/shared/lib/queryClient';
import type { ListParams } from '@/core/api/useCrudResource';
import { MENU_PERMISSIONS_BASE_PATH, MENU_PERMISSIONS_KEY, type CreateMenuPermissionRequest, type MenuPermission } from '../api/menuPermissionsApi';

export function useMenuPermissions(params: ListParams = {}) {
  return useQuery({
    queryKey: [MENU_PERMISSIONS_KEY, 'list', params],
    queryFn: () => apiClient.get<ApiResponse<MenuPermission[]>>(MENU_PERMISSIONS_BASE_PATH, { params }).then(unwrapPage),
    staleTime: STALE.SHORT,
    placeholderData: (prev) => prev,
  });
}

export function useCreateMenuPermission() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (body: CreateMenuPermissionRequest) =>
      apiClient.post<ApiResponse<MenuPermission>>(MENU_PERMISSIONS_BASE_PATH, body).then((r) => r.data.data),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: [MENU_PERMISSIONS_KEY] });
      notify('Menu permission granted', 'success');
    },
  });
}

export function useDeleteMenuPermission() {
  const queryClient = useQueryClient();
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (id: string) => apiClient.delete<ApiResponse<void>>(`${MENU_PERMISSIONS_BASE_PATH}/${id}`).then((r) => r.data.data),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: [MENU_PERMISSIONS_KEY] });
      notify('Menu permission revoked', 'success');
    },
  });
}
