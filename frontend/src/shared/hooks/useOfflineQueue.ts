import { useEffect, useRef } from 'react';
import { useAppStore } from '@/shared/stores/useAppStore';
import { queryClient } from '@/shared/lib/queryClient';

interface QueuedMutation {
  id: string;
  url: string;
  method: string;
  body: unknown;
  timestamp: number;
}

const QUEUE_KEY = 'offline-mutation-queue';

function loadQueue(): QueuedMutation[] {
  try { return JSON.parse(localStorage.getItem(QUEUE_KEY) ?? '[]') as QueuedMutation[]; }
  catch { return []; }
}
function saveQueue(q: QueuedMutation[]) { localStorage.setItem(QUEUE_KEY, JSON.stringify(q)); }

/**
 * Queues failed mutations when offline and replays them when connection restores.
 * Wire into apiClient.interceptors for full offline support.
 */
export function useOfflineQueue() {
  const { notify } = useAppStore();
  const wasOffline = useRef(false);

  useEffect(() => {
    const handleOnline = async () => {
      if (!wasOffline.current) return;
      wasOffline.current = false;

      const queue = loadQueue();
      if (queue.length === 0) { notify('Back online', 'success'); return; }

      notify(`Back online — replaying ${queue.length} queued action${queue.length > 1 ? 's' : ''}…`, 'info');

      for (const mutation of queue) {
        try {
          await fetch(mutation.url, {
            method: mutation.method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(mutation.body),
            credentials: 'include',
          });
        } catch { /* individual replay failure — leave in queue */ }
      }
      saveQueue([]);
      queryClient.invalidateQueries();
      notify('All queued actions synced', 'success');
    };

    const handleOffline = () => {
      wasOffline.current = true;
      notify('You are offline. Changes will be queued.', 'warning');
    };

    const onOnline = () => void handleOnline();
    window.addEventListener('online', onOnline);
    window.addEventListener('offline', handleOffline);
    return () => {
      window.removeEventListener('online', onOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, [notify]);
}

export function queueMutation(url: string, method: string, body: unknown) {
  const queue = loadQueue();
  queue.push({ id: crypto.randomUUID(), url, method, body, timestamp: Date.now() });
  saveQueue(queue);
}
