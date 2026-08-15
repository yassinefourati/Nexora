import * as Sentry from '@sentry/react';
import { env } from './env';

const dsn = (import.meta.env as Record<string, string>).VITE_SENTRY_DSN;

export function initSentry() {
  if (!dsn || env.DEV) return;
  Sentry.init({
    dsn,
    integrations: [Sentry.browserTracingIntegration(), Sentry.replayIntegration()],
    tracesSampleRate: 0.2,
    replaysOnErrorSampleRate: 1.0,
  });
}

export function setSentryUser(user: { id: number; email: string; role: string } | null) {
  Sentry.setUser(user ? { id: String(user.id), email: user.email, role: user.role } : null);
}
