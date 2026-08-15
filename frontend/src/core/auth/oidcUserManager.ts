import { UserManager, WebStorageStateStore } from 'oidc-client-ts';
import { env } from '@/core/config/env';

/**
 * Single UserManager instance for the whole app. oidc-client-ts owns PKCE
 * code_verifier generation, state, silent renew via refresh_token, and
 * persists the OIDC user session in sessionStorage (not localStorage, so a
 * closed tab doesn't leave tokens lying around indefinitely).
 */
export const oidcUserManager = new UserManager({
  authority: env.VITE_OIDC_AUTHORITY,
  client_id: env.VITE_OIDC_CLIENT_ID,
  redirect_uri: env.VITE_OIDC_REDIRECT_URI,
  post_logout_redirect_uri: env.VITE_OIDC_POST_LOGOUT_REDIRECT_URI,
  response_type: 'code',
  scope: 'openid profile email',
  userStore: new WebStorageStateStore({ store: window.sessionStorage }),
  automaticSilentRenew: true,
  monitorSession: false,
  loadUserInfo: false,
});
