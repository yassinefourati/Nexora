import { describe, it, expect, beforeEach, vi } from 'vitest';

vi.mock('@/core/api/client', () => ({
  default: { interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } } },
  setupInterceptors: vi.fn(),
}));

const { mockSigninRedirect, mockSignoutRedirect, mockSigninRedirectCallback, mockGetUser } = vi.hoisted(() => ({
  mockSigninRedirect: vi.fn(),
  mockSignoutRedirect: vi.fn(),
  mockSigninRedirectCallback: vi.fn(),
  mockGetUser: vi.fn(),
}));

const { mockRemoveUser } = vi.hoisted(() => ({ mockRemoveUser: vi.fn() }));

vi.mock('@/core/auth/oidcUserManager', () => ({
  oidcUserManager: {
    signinRedirect: mockSigninRedirect,
    signoutRedirect: mockSignoutRedirect,
    signinRedirectCallback: mockSigninRedirectCallback,
    signinSilent: vi.fn(),
    getUser: mockGetUser,
    removeUser: mockRemoveUser,
    stopSilentRenew: vi.fn(),
    events: {
      addUserLoaded: vi.fn(),
      addUserUnloaded: vi.fn(),
      addSilentRenewError: vi.fn(),
    },
  },
}));

vi.mock('@/shared/stores/useWebSocketStore', () => ({
  useWebSocketStore: { getState: () => ({ disconnect: vi.fn() }) },
}));

import { useAuthStore } from '@/core/auth/stores/useAuthStore';

function fakeOidcUser(roles: string[]) {
  return {
    profile: { sub: 'user-1', preferred_username: 'alice', email: 'alice@example.com', name: 'Alice', roles },
    access_token: 'tok-123',
    expired: false,
  };
}

describe('useAuthStore', () => {
  beforeEach(() => {
    useAuthStore.setState({ user: null, accessToken: null, isAuthenticated: false, isLoading: false, isInitialized: false });
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  it('login triggers the OIDC redirect', async () => {
    await useAuthStore.getState().login();
    expect(mockSigninRedirect).toHaveBeenCalledOnce();
  });

  it('handleCallback sets user and access token from the OIDC response', async () => {
    mockSigninRedirectCallback.mockResolvedValueOnce(fakeOidcUser(['admin']));
    await useAuthStore.getState().handleCallback();
    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.accessToken).toBe('tok-123');
    expect(state.user?.roles).toEqual(['admin']);
  });

  it('init restores a non-expired session from storage', async () => {
    mockGetUser.mockResolvedValueOnce(fakeOidcUser(['user']));
    await useAuthStore.getState().init();
    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.isInitialized).toBe(true);
  });

  it('init leaves the session unauthenticated when none is stored', async () => {
    mockGetUser.mockResolvedValueOnce(null);
    await useAuthStore.getState().init();
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
  });

  it('logout clears local state and triggers the OIDC signout redirect', async () => {
    useAuthStore.setState({ user: { sub: 'u', username: 'a', email: 'a@a.com', name: 'A', roles: ['admin'] }, accessToken: 'tok', isAuthenticated: true });
    await useAuthStore.getState().logout();
    expect(useAuthStore.getState().user).toBeNull();
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(mockSignoutRedirect).toHaveBeenCalledOnce();
  });

  it('logout does not call removeUser itself — signoutRedirect needs the id_token_hint', async () => {
    // Calling removeUser() before signoutRedirect() strips the id_token Keycloak
    // needs to skip its interactive logout confirmation screen (confirmed live).
    await useAuthStore.getState().logout();
    expect(mockRemoveUser).not.toHaveBeenCalled();
  });

  it('init ignores and purges a stale user found after a logout marker was left behind', async () => {
    // Simulates the real race: a background token refresh lands after logout
    // navigated away, writing a fresh user back into storage before the next
    // page's init() runs.
    sessionStorage.setItem('auth.justLoggedOut', '1');
    mockGetUser.mockResolvedValueOnce(fakeOidcUser(['admin']));
    await useAuthStore.getState().init();
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(mockRemoveUser).toHaveBeenCalledOnce();
  });

  it('handleCallback clears the logout marker so a genuine new login is trusted again', async () => {
    sessionStorage.setItem('auth.justLoggedOut', '1');
    mockSigninRedirectCallback.mockResolvedValueOnce(fakeOidcUser(['admin']));
    await useAuthStore.getState().handleCallback();
    expect(sessionStorage.getItem('auth.justLoggedOut')).toBeNull();
    expect(useAuthStore.getState().isAuthenticated).toBe(true);
  });
});
