import { create } from 'zustand';
type Severity = 'success' | 'error' | 'warning' | 'info';
interface AppState { loading: boolean; snackbar: { open: boolean; message: string; severity: Severity }; setLoading: (v: boolean) => void; notify: (msg: string, sev?: Severity) => void; closeSnackbar: () => void; }
export const useAppStore = create<AppState>((set) => ({
  loading: false, snackbar: { open: false, message: '', severity: 'info' },
  setLoading: (v) => set({ loading: v }),
  notify: (message, severity = 'info') => set({ snackbar: { open: true, message, severity } }),
  closeSnackbar: () => set((s) => ({ snackbar: { ...s.snackbar, open: false } })),
}));
