import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';
import { visualizer } from 'rollup-plugin-visualizer';

export default defineConfig(({ mode }) => ({
  plugins: [
    react(),
    // Bundle analysis: npm run build -- --mode analyze → opens stats.html
    mode === 'analyze' && visualizer({ open: true, filename: 'stats.html', gzipSize: true, brotliSize: true }),
  ].filter(Boolean),
  resolve: { alias: { '@': resolve(import.meta.dirname, 'src') } },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.ts',
    // Playwright specs live in e2e/ and are run by `npx playwright test`, not Vitest
    exclude: ['e2e/**', 'node_modules/**', 'dist/**'],
  },
  define: {
    global: 'window',
  },
}));

