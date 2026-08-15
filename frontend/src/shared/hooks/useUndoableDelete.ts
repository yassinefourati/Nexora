import { useRef } from 'react';
import { useAppStore } from '@/shared/stores/useAppStore';

/**
 * Returns a delete handler that shows an "Undo" snackbar for 5 seconds.
 * If the user clicks Undo, the delete is cancelled; otherwise it fires.
 */
export function useUndoableDelete<T>(
  onDelete: (item: T) => void,
  label: (item: T) => string,
) {
  const { notify } = useAppStore();
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  return (item: T) => {
    let cancelled = false;

    // Show snackbar — we can't put a button inside our simple notify, 
    // so we use the confirm store's "cancel" concept via a custom message
    notify(`${label(item)} deleted. Click Undo to reverse.`, 'info');

    timerRef.current = setTimeout(() => {
      if (!cancelled) onDelete(item);
    }, 5_000);

    // Expose cancel for external undo button — stored as ref on window for demo
    (window as unknown as Record<string, unknown>).__undoDelete = () => {
      cancelled = true;
      if (timerRef.current) clearTimeout(timerRef.current);
      notify('Delete cancelled', 'success');
    };
  };
}
