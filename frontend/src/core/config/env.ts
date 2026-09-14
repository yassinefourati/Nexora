interface Env {
  VITE_API_URL: string;
  VITE_APP_TITLE: string;
  VITE_IDLE_TIMEOUT_MS: number;
  VITE_SESSION_WARNING_MS: number;
  VITE_OIDC_AUTHORITY: string;
  VITE_OIDC_CLIENT_ID: string;
  VITE_OIDC_REDIRECT_URI: string;
  VITE_OIDC_POST_LOGOUT_REDIRECT_URI: string;
  VITE_FLAGS: Record<string, boolean>;
  DEV: boolean;
}

/**
 * Runtime configuration injected by the container entrypoint (frontend/docker/40-runtime-config.sh)
 * as window.__APP_CONFIG__, so one built image serves every environment. Build-time VITE_* values
 * remain the fallback for `npm run dev` and for images built with explicit args.
 */
function runtimeConfig(): Record<string, string> {
  try {
    return ((globalThis as { __APP_CONFIG__?: Record<string, string> }).__APP_CONFIG__) ?? {};
  } catch {
    return {};
  }
}

function requireVar(key: string, fallback?: string): string {
  const val = runtimeConfig()[key] ?? (import.meta.env as Record<string, string>)[key] ?? fallback;
  if (!val && !import.meta.env.DEV) {
    throw new Error(`[env] Required variable ${key} is not defined. Set it before building.`);
  }
  return val ?? '';
}

function validateEnv(): Env {
  return {
    VITE_API_URL:             requireVar('VITE_API_URL', '/api/v1'),
    VITE_APP_TITLE:           requireVar('VITE_APP_TITLE', 'Admin Panel'),
    VITE_IDLE_TIMEOUT_MS:     Number(requireVar('VITE_IDLE_TIMEOUT_MS', '900000')),  // 15 min
    VITE_SESSION_WARNING_MS:  Number(requireVar('VITE_SESSION_WARNING_MS', '60000')), // 60 s
    VITE_OIDC_AUTHORITY:                requireVar('VITE_OIDC_AUTHORITY', 'http://localhost:9090/realms/fourati-realm'),
    VITE_OIDC_CLIENT_ID:                requireVar('VITE_OIDC_CLIENT_ID', 'admin-frontend'),
    VITE_OIDC_REDIRECT_URI:             requireVar('VITE_OIDC_REDIRECT_URI', 'http://localhost:5173/auth/callback'),
    VITE_OIDC_POST_LOGOUT_REDIRECT_URI: requireVar('VITE_OIDC_POST_LOGOUT_REDIRECT_URI', 'http://localhost:5173/login'),
    VITE_FLAGS: (() => { try { const f = runtimeConfig().VITE_FLAGS ?? (import.meta.env as Record<string,string>).VITE_FLAGS; return f ? JSON.parse(f) as Record<string,boolean> : {}; } catch { return {}; } })(),
    DEV: import.meta.env.DEV,
  };
}

export const env = validateEnv();
