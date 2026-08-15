import { useEffect, useRef } from 'react';

interface SSEOptions {
  url: string;
  onMessage: (data: unknown) => void;
  enabled?: boolean;
}

/**
 * Server-Sent Events hook.
 * In dev MSW intercepts the endpoint and returns polling fallback.
 * In production, swap to a real SSE endpoint.
 */
export function useSSE({ url, onMessage, enabled = true }: SSEOptions) {
  const cbRef = useRef(onMessage);

  useEffect(() => {
    cbRef.current = onMessage;
  }, [onMessage]);

  useEffect(() => {
    if (!enabled) return;

    // MSW doesn't fully support SSE in browser — use polling fallback in dev
    if (import.meta.env.DEV) {
      const id = setInterval(async () => {
        try {
          const res = await fetch(url);
          if (res.ok) cbRef.current(await res.json());
        } catch { /* ignore */ }
      }, 10_000);
      return () => clearInterval(id);
    }

    const es = new EventSource(url, { withCredentials: true });
    es.onmessage = (e) => {
      try { cbRef.current(JSON.parse(e.data as string)); } catch { /* ignore */ }
    };
    return () => es.close();
  }, [url, enabled]);
}
