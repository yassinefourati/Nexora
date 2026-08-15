import { create } from 'zustand';
import type { User as OidcUser } from 'oidc-client-ts';
import { oidcUserManager } from '@/core/auth/oidcUserManager';
import { rolesFromOidcUser } from '@/core/auth/decodeRoles';
import { setupInterceptors } from '@/core/api/client';

export interface AuthUser {
  sub: string;
  username: string;
  email: string;
  name: string;
  roles: string[];
}

interface AuthState {
  user: AuthUser | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  /** Populated once the initial silent user-load from sessionStorage completes. */
  isInitialized: boolean;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  handleCallback: () => Promise<void>;
  init: () => Promise<void>;
}

function toAuthUser(oidcUser: OidcUser): AuthUser {
  const profile = oidcUser.profile as Record<string, unknown>;
  return {
    sub: oidcUser.profile.sub,
    username: (profile.preferred_username as string) ?? oidcUser.profile.sub,
    email: (profile.email as string) ?? '',
    name: (profile.name as string) ?? (profile.preferred_username as string) ?? '',
    roles: rolesFromOidcUser(oidcUser),
  };
}

/**
 * Set the moment logout() is called, read once on the next page load, then
 * cleared. Guards against a real race in oidc-client-ts: automaticSilentRenew
 * schedules background token refreshes independent of any timer we can cancel
 * synchronously, so a refresh already in flight when logout() runs can
 * resolve *after* the redirect to Keycloak and back — writing the "removed"
 * user straight back into sessionStorage (confirmed live: sessionStorage held
 * a fresh, non-expired OIDC user immediately after landing on /login,
 * sometimes over a second after the redirect completed). Because that write
 * can land after the full-page navigation, an in-memory flag doesn't survive
 * it — this needs to be in storage itself.
 */
const LOGOUT_MARKER_KEY = 'auth.justLoggedOut';

function markLoggingOut() {
  sessionStorage.setItem(LOGOUT_MARKER_KEY, '1');
}

function isLoggingOut(): boolean {
  return sessionStorage.getItem(LOGOUT_MARKER_KEY) === '1';
}

/** Only a genuine new sign-in (handleCallback) clears the marker — a stray
 * background refresh landing seconds after logout must keep being ignored,
 * not just the very next page load. */
function clearLogoutMarker() {
  sessionStorage.removeItem(LOGOUT_MARKER_KEY);
}

export const useAuthStore = create<AuthState>()((set, get) => ({
  user: null,
  accessToken: null,
  isAuthenticated: false,
  isLoading: false,
  isInitialized: false,

  /** Kicks off the Authorization Code + PKCE redirect to Keycloak. */
  login: async () => {
    await oidcUserManager.signinRedirect();
  },

  logout: async () => {
    markLoggingOut();
    set({ user: null, accessToken: null, isAuthenticated: false });
    await import('@/shared/stores/useWebSocketStore')
      .then(({ useWebSocketStore }) => useWebSocketStore.getState().disconnect())
      .catch(() => {});
    oidcUserManager.stopSilentRenew();
    // signoutRedirect() reads the stored user's id_token to build the
    // id_token_hint Keycloak needs to skip its logout confirmation screen,
    // then clears local storage itself before navigating — do not call
    // removeUser() first, or there is no id_token left to hint with.
    await oidcUserManager.signoutRedirect();
  },

  /** Completes the redirect-back leg of the Authorization Code flow (/auth/callback). */
  handleCallback: async () => {
    clearLogoutMarker();
    set({ isLoading: true });
    try {
      const oidcUser = await oidcUserManager.signinRedirectCallback();
      set({
        user: toAuthUser(oidcUser),
        accessToken: oidcUser.access_token,
        isAuthenticated: true,
      });
    } finally {
      set({ isLoading: false });
    }
  },

  /** Restores a session already present in sessionStorage on full page reload. */
  init: async () => {
    if (get().isInitialized) return;
    set({ isLoading: true });
    try {
      if (isLoggingOut()) {
        // A refresh that raced the logout redirect may have written a fresh
        // user back into storage after logout completed — purge it rather
        // than trust it. The marker stays set (see clearLogoutMarker) so a
        // late write arriving after THIS check still gets caught by
        // addUserLoaded below, until the user genuinely logs back in.
        const staleUser = await oidcUserManager.getUser();
        if (staleUser) await oidcUserManager.removeUser();
        return;
      }
      const oidcUser = await oidcUserManager.getUser();
      if (oidcUser && !oidcUser.expired) {
        set({ user: toAuthUser(oidcUser), accessToken: oidcUser.access_token, isAuthenticated: true });
      }
    } finally {
      set({ isLoading: false, isInitialized: true });
    }
  },
}));

oidcUserManager.events.addUserLoaded((oidcUser) => {
  if (isLoggingOut()) {
    void oidcUserManager.removeUser();
    return;
  }
  useAuthStore.setState({ user: toAuthUser(oidcUser), accessToken: oidcUser.access_token, isAuthenticated: true });
});

oidcUserManager.events.addUserUnloaded(() => {
  useAuthStore.setState({ user: null, accessToken: null, isAuthenticated: false });
});

oidcUserManager.events.addSilentRenewError(() => {
  useAuthStore.setState({ user: null, accessToken: null, isAuthenticated: false });
});

setupInterceptors(
  () => useAuthStore.getState().accessToken ?? undefined,
  () => useAuthStore.getState().logout(),
);
