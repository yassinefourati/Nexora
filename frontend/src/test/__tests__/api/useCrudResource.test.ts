import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { createQueryWrapper } from '@/test/utils/queryWrapper';

const { mockGet, mockPost, mockPut, mockDelete } = vi.hoisted(() => ({
  mockGet: vi.fn(), mockPost: vi.fn(), mockPut: vi.fn(), mockDelete: vi.fn(),
}));

vi.mock('@/core/api/client', () => ({
  default: { get: mockGet, post: mockPost, put: mockPut, delete: mockDelete },
  setupInterceptors: vi.fn(),
}));

const notifyMock = vi.fn();
vi.mock('@/shared/stores/useAppStore', () => ({
  useAppStore: () => ({ notify: notifyMock }),
}));

import { useCrudResource } from '@/core/api/useCrudResource';

interface Widget { id: string; name: string; }

function envelope<T>(data: T, pagination?: object) {
  return { data: { success: true, status: 200, message: 'OK', timestamp: '', correlationId: '', data, ...(pagination ? { pagination } : {}) } };
}

describe('useCrudResource', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('useList unwraps items and pagination from the real envelope', async () => {
    mockGet.mockResolvedValueOnce(envelope<Widget[]>(
      [{ id: '1', name: 'Alpha' }],
      { page: 0, size: 20, totalElements: 1, totalPages: 1, first: true, last: true },
    ));

    const { result } = renderHook(
      () => useCrudResource<Widget>('/widgets', 'widgets').useList({ page: 0, size: 20 }),
      { wrapper: createQueryWrapper() },
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data?.items).toEqual([{ id: '1', name: 'Alpha' }]);
    expect(result.current.data?.pagination.totalElements).toBe(1);
    expect(mockGet).toHaveBeenCalledWith('/widgets', { params: { page: 0, size: 20 } });
  });

  it('useOne is disabled until an id is provided', () => {
    const { result } = renderHook(
      () => useCrudResource<Widget>('/widgets', 'widgets').useOne(undefined),
      { wrapper: createQueryWrapper() },
    );
    expect(result.current.fetchStatus).toBe('idle');
    expect(mockGet).not.toHaveBeenCalled();
  });

  it('useOne fetches and unwraps a single resource once an id is set', async () => {
    mockGet.mockResolvedValueOnce(envelope<Widget>({ id: '1', name: 'Alpha' }));
    const { result } = renderHook(
      () => useCrudResource<Widget>('/widgets', 'widgets').useOne('1'),
      { wrapper: createQueryWrapper() },
    );
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual({ id: '1', name: 'Alpha' });
  });

  it('useCreate posts the body and invalidates the list on success', async () => {
    mockPost.mockResolvedValueOnce(envelope<Widget>({ id: '2', name: 'Beta' }));
    const { result } = renderHook(
      () => useCrudResource<Widget, Partial<Widget>>('/widgets', 'widgets').useCreate(),
      { wrapper: createQueryWrapper() },
    );

    await act(async () => { await result.current.mutateAsync({ name: 'Beta' }); });

    expect(mockPost).toHaveBeenCalledWith('/widgets', { name: 'Beta' });
    expect(notifyMock).toHaveBeenCalledWith('Created successfully', 'success');
  });

  it('useUpdate puts to the resource id and notifies on success', async () => {
    mockPut.mockResolvedValueOnce(envelope<Widget>({ id: '1', name: 'Alpha v2' }));
    const { result } = renderHook(
      () => useCrudResource<Widget, Partial<Widget>, Partial<Widget>>('/widgets', 'widgets').useUpdate(),
      { wrapper: createQueryWrapper() },
    );

    await act(async () => { await result.current.mutateAsync({ id: '1', body: { name: 'Alpha v2' } }); });

    expect(mockPut).toHaveBeenCalledWith('/widgets/1', { name: 'Alpha v2' });
    expect(notifyMock).toHaveBeenCalledWith('Updated successfully', 'success');
  });

  it('useRemove deletes the resource id and notifies on success', async () => {
    mockDelete.mockResolvedValueOnce(envelope<void>(undefined));
    const { result } = renderHook(
      () => useCrudResource<Widget>('/widgets', 'widgets').useRemove(),
      { wrapper: createQueryWrapper() },
    );

    await act(async () => { await result.current.mutateAsync('1'); });

    expect(mockDelete).toHaveBeenCalledWith('/widgets/1');
    expect(notifyMock).toHaveBeenCalledWith('Deleted successfully', 'success');
  });
});
