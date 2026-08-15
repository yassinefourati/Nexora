import axios from 'axios';
import { env } from '@/core/config/env';
import { oidcUserManager } from '@/core/auth/oidcUserManager';

/**
 * Auth model: stateless JWT resource server. The backend has no login,
 * refresh, or CSRF endpoints — the access token comes from Keycloak via
 * Authorization Code + PKCE (see core/auth/oidcUserManager.ts) and is sent
 * as a Bearer token on every request. oidc-client-ts silently renews the
 * token in the background using the refresh_token before it expires; the
 * response interceptor below only needs to handle the case where a request
 * still lands with an expired token (e.g. renew failed / raced).
 */
const apiClient = axios.create({
  baseURL: env.VITE_API_URL,
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' },
});

let renewing: Promise<string | null> | null = null;

export function setupInterceptors(
  getAccessToken: () => string | undefined,
  onLogout: () => void,
) {
  apiClient.interceptors.request.use((config) => {
    const token = getAccessToken();
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  });

  apiClient.interceptors.response.use(
    (res) => res,
    async (error: { response?: { status: number }; config: { _retry?: boolean; headers: Record<string, string> } }) => {
      const original = error.config;

      // Only attempt a silent renew when the request actually carried a token
      // that the backend rejected (e.g. it expired mid-flight). A request with
      // no Authorization header at all means there's no session to renew —
      // most commonly right after a deliberate logout — and calling
      // signinSilent() here would use Keycloak's still-live browser SSO
      // cookie to silently sign the user back in, undoing the logout.
      const hadToken = Boolean(getAccessToken());

      if (error.response?.status === 401 && !original._retry && hadToken) {
        original._retry = true;
        renewing ??= oidcUserManager.signinSilent()
          .then((u) => u?.access_token ?? null)
          .catch(() => null)
          .finally(() => { renewing = null; });

        const newToken = await renewing;
        if (!newToken) {
          onLogout();
          return Promise.reject(error);
        }
        original.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(original);
      }

      return Promise.reject(error);
    }
  );
}

export default apiClient;
