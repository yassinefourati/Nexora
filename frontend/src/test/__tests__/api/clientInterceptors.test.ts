import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/msw/server';
import { API, envelope } from '@/test/msw/handlers';

// The interceptor renews through oidc-client-ts; stub the manager so no real OIDC flow runs.
const signinSilent = vi.fn();
vi.mock('@/core/auth/oidcUserManager', () => ({
  oidcUserManager: { signinSilent: (...args: unknown[]) => signinSilent(...args) },
}));

const { default: apiClient, setupInterceptors } = await import('@/core/api/client');

/**
 * The hardest logic in the API layer: on a 401 with a token present, renew
 * exactly once (coalescing concurrent requests into one signinSilent call),
 * retry the original request with the new token, and log out if renewal fails.
 * A 429 must surface the Retry-After value to the user.
 */
describe('apiClient interceptors', () => {
  let token: string | undefined;
  const onLogout = vi.fn();
  const notify = vi.fn();

  beforeAll(() => {
    server.listen({ onUnhandledRequest: 'error' });
    apiClient.defaults.baseURL = API;
    setupInterceptors(() => token, onLogout);
  });
  afterEach(() => { server.resetHandlers(); vi.clearAllMocks(); });
  afterAll(() => server.close());

  beforeEach(async () => {
    token = 'old-token';
    const store = await import('@/shared/stores/useAppStore');
    store.useAppStore.setState({ notify });
  });

  it('attaches the bearer token to every request', async () => {
    let seen = '';
    server.use(http.get(`${API}/ping`, ({ request }) => {
      seen = request.headers.get('authorization') ?? '';
      return HttpResponse.json(envelope('pong'));
    }));

    await apiClient.get('/ping');

    expect(seen).toBe('Bearer old-token');
  });

  it('renews once on 401 and retries with the new token', async () => {
    const authHeaders: string[] = [];
    server.use(http.get(`${API}/secure`, ({ request }) => {
      const auth = request.headers.get('authorization') ?? '';
      authHeaders.push(auth);
      return auth === 'Bearer new-token'
        ? HttpResponse.json(envelope('ok'))
        : new HttpResponse(null, { status: 401 });
    }));
    signinSilent.mockImplementation(async () => { token = 'new-token'; return { access_token: 'new-token' }; });

    const res = await apiClient.get('/secure');

    expect(res.data.data).toBe('ok');
    expect(signinSilent).toHaveBeenCalledTimes(1);
    expect(authHeaders).toEqual(['Bearer old-token', 'Bearer new-token']);
    expect(onLogout).not.toHaveBeenCalled();
  });

  it('coalesces concurrent 401s into a single renewal', async () => {
    server.use(http.get(`${API}/a`, ({ request }) =>
      request.headers.get('authorization') === 'Bearer new-token' ? HttpResponse.json(envelope('a')) : new HttpResponse(null, { status: 401 })));
    server.use(http.get(`${API}/b`, ({ request }) =>
      request.headers.get('authorization') === 'Bearer new-token' ? HttpResponse.json(envelope('b')) : new HttpResponse(null, { status: 401 })));
    signinSilent.mockImplementation(() => new Promise((resolve) => setTimeout(() => { token = 'new-token'; resolve({ access_token: 'new-token' }); }, 20)));

    const [a, b] = await Promise.all([apiClient.get('/a'), apiClient.get('/b')]);

    expect(a.data.data).toBe('a');
    expect(b.data.data).toBe('b');
    expect(signinSilent).toHaveBeenCalledTimes(1);
  });

  it('logs out when renewal fails, and does not retry forever', async () => {
    let calls = 0;
    server.use(http.get(`${API}/secure`, () => { calls++; return new HttpResponse(null, { status: 401 }); }));
    signinSilent.mockRejectedValue(new Error('refresh expired'));

    await expect(apiClient.get('/secure')).rejects.toBeTruthy();

    expect(onLogout).toHaveBeenCalledTimes(1);
    expect(calls).toBe(1);
  });

  it('does not attempt a renewal when the request carried no token (post-logout)', async () => {
    token = undefined;
    server.use(http.get(`${API}/secure`, () => new HttpResponse(null, { status: 401 })));

    await expect(apiClient.get('/secure')).rejects.toBeTruthy();

    expect(signinSilent).not.toHaveBeenCalled();
    expect(onLogout).not.toHaveBeenCalled();
  });

  it('surfaces Retry-After on 429', async () => {
    server.use(http.get(`${API}/busy`, () => new HttpResponse(null, { status: 429, headers: { 'Retry-After': '42' } })));

    await expect(apiClient.get('/busy')).rejects.toBeTruthy();
    await vi.waitFor(() => expect(notify).toHaveBeenCalledWith(expect.stringContaining('42s'), 'warning'));
  });
});
