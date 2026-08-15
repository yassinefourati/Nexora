import { useIsFetching, useIsMutating } from '@tanstack/react-query';
import { useEffect } from 'react';
import { useAppStore } from '@/shared/stores/useAppStore';

/**
 * Automatically shows the top loading bar whenever any query is fetching
 * or any mutation is in-flight. Wire once in AppProviders.
 */
export function useLoadingBar() {
  const fetching  = useIsFetching();
  const mutating  = useIsMutating();
  const { setLoading } = useAppStore();
  useEffect(() => { setLoading(fetching > 0 || mutating > 0); }, [fetching, mutating, setLoading]);
}
