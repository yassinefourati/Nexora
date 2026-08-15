import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { ThemeMode } from '@/core/theme';
interface RecentPage { path: string; label: string; }
interface UIState {
  sidebarOpen: boolean; theme: ThemeMode; language: 'en' | 'fr'; seenOnboarding: boolean; recentPages: RecentPage[];
  toggleSidebar: () => void; toggleTheme: () => void; setLanguage: (l: 'en' | 'fr') => void; setSeenOnboarding: () => void;
  addRecentPage: (path: string, label: string) => void;
}
export const useUIStore = create<UIState>()(persist(
  (set) => ({
    sidebarOpen: true, theme: 'light', language: 'en', seenOnboarding: false, recentPages: [],
    toggleSidebar: () => set((s) => ({ sidebarOpen: !s.sidebarOpen })),
    toggleTheme: () => set((s) => ({ theme: s.theme === 'light' ? 'blue' : s.theme === 'blue' ? 'dark' : 'light' })),
    setLanguage: (language) => set({ language }),
    setSeenOnboarding: () => set({ seenOnboarding: true }),
    addRecentPage: (path, label) => set((s) => ({ recentPages: [{ path, label }, ...s.recentPages.filter((p) => p.path !== path)].slice(0, 5) })),
  }),
  { name: 'ui-storage' }
));
