import { useEffect, useRef, useCallback } from 'react';

interface UseIdleTimerOptions {
  timeout: number;          // ms until warning
  warningTime: number;      // ms to show warning before logout
  onWarning: () => void;
  onIdle: () => void;
}

export function useIdleTimer({ timeout, warningTime, onWarning, onIdle }: UseIdleTimerOptions) {
  const warningTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const idleTimerRef    = useRef<ReturnType<typeof setTimeout> | null>(null);

  const reset = useCallback(() => {
    if (warningTimerRef.current) clearTimeout(warningTimerRef.current);
    if (idleTimerRef.current)    clearTimeout(idleTimerRef.current);

    warningTimerRef.current = setTimeout(() => {
      onWarning();
      idleTimerRef.current = setTimeout(onIdle, warningTime);
    }, timeout - warningTime);
  }, [timeout, warningTime, onWarning, onIdle]);

  useEffect(() => {
    const events = ['mousemove', 'mousedown', 'keydown', 'touchstart', 'scroll'];
    events.forEach((e) => window.addEventListener(e, reset, { passive: true }));
    reset();
    return () => {
      events.forEach((e) => window.removeEventListener(e, reset));
      if (warningTimerRef.current) clearTimeout(warningTimerRef.current);
      if (idleTimerRef.current)    clearTimeout(idleTimerRef.current);
    };
  }, [reset]);
}
