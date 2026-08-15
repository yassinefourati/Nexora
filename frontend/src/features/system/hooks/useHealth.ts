import { useQuery } from '@tanstack/react-query';
import { getHealth } from '../api/healthApi';
import { STALE } from '@/shared/lib/queryClient';

export function useHealth() {
  return useQuery({
    queryKey: ['system', 'actuator-health'],
    queryFn: getHealth,
    staleTime: STALE.REALTIME,
    refetchInterval: STALE.REALTIME,
    retry: false,
  });
}
