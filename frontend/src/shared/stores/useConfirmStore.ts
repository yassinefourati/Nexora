import { create } from 'zustand';
export interface ConfirmOptions { title: string; message: string; confirmLabel?: string; severity?: 'error' | 'warning' | 'info'; onConfirm: () => void; }
interface ConfirmState { open: boolean; options: ConfirmOptions | null; confirm: (o: ConfirmOptions) => void; close: () => void; }
export const useConfirmStore = create<ConfirmState>((set) => ({ open: false, options: null, confirm: (options) => set({ open: true, options }), close: () => set({ open: false, options: null }) }));
