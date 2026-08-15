import { describe, it, expect, beforeEach } from 'vitest';
import { useAppStore } from '@/shared/stores/useAppStore';
describe('useAppStore', () => {
  beforeEach(() => useAppStore.setState({ loading: false, snackbar: { open: false, message: '', severity: 'info' } }));
  it('setLoading(true) sets loading flag', () => { useAppStore.getState().setLoading(true); expect(useAppStore.getState().loading).toBe(true); });
  it('setLoading(false) clears loading', () => { useAppStore.setState({ loading: true }); useAppStore.getState().setLoading(false); expect(useAppStore.getState().loading).toBe(false); });
  it('notify opens snackbar with message and severity', () => { useAppStore.getState().notify('Error!', 'error'); const { snackbar } = useAppStore.getState(); expect(snackbar.open).toBe(true); expect(snackbar.message).toBe('Error!'); expect(snackbar.severity).toBe('error'); });
  it('notify defaults severity to info', () => { useAppStore.getState().notify('Hello'); expect(useAppStore.getState().snackbar.severity).toBe('info'); });
  it('closeSnackbar closes snackbar', () => { useAppStore.getState().notify('test', 'success'); useAppStore.getState().closeSnackbar(); expect(useAppStore.getState().snackbar.open).toBe(false); });
});
