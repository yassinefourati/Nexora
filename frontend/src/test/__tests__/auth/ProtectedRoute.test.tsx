import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/msw/server';
import { API, envelope } from '@/test/msw/handlers';
import { createQueryWrapper } from '@/test/utils/queryWrapper';

vi.mock('@/core/auth/oidcUserManager', () => ({
  oidcUserManager: {
    signinSilent: vi.fn(), signinRedirect: vi.fn(), signoutRedirect: vi.fn(), stopSilentRenew: vi.fn(),
    getUser: vi.fn().mockResolvedValue(null), removeUser: vi.fn(),
    events: { addUserLoaded: vi.fn(), addUserUnloaded: vi.fn(), addSilentRenewError: vi.fn() },
  },
}));

const { useAuthStore } = await import('@/core/auth/stores/useAuthStore');
const { default: apiClient } = await import('@/core/api/client');
const { default: ProtectedRoute } = await import('@/core/auth/guards/ProtectedRoute');

function renderAt(path: string, element: React.ReactNode) {
  const Wrapper = createQueryWrapper();
  return render(
    <Wrapper>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/login" element={<div>login page</div>} />
          <Route path="/403" element={<div>forbidden page</div>} />
          <Route path={path} element={element} />
        </Routes>
      </MemoryRouter>
    </Wrapper>,
  );
}

describe('ProtectedRoute', () => {
  beforeAll(() => { server.listen({ onUnhandledRequest: 'error' }); apiClient.defaults.baseURL = API; });
  afterEach(() => { server.resetHandlers(); });
  afterAll(() => server.close());

  it('shows a progress bar until the auth store is initialised', () => {
    useAuthStore.setState({ isInitialized: false, isAuthenticated: false, accessToken: null });
    renderAt('/users', <ProtectedRoute><div>secret</div></ProtectedRoute>);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
    expect(screen.queryByText('secret')).not.toBeInTheDocument();
  });

  it('redirects an unauthenticated visitor to /login', () => {
    useAuthStore.setState({ isInitialized: true, isAuthenticated: false, accessToken: null });
    renderAt('/users', <ProtectedRoute><div>secret</div></ProtectedRoute>);

    expect(screen.getByText('login page')).toBeInTheDocument();
  });

  it('renders the page when the user holds the required permission', async () => {
    useAuthStore.setState({ isInitialized: true, isAuthenticated: true, accessToken: 't' });
    renderAt('/users', <ProtectedRoute resource="user" action="read"><div>secret</div></ProtectedRoute>);

    expect(await screen.findByText('secret')).toBeInTheDocument();
  });

  it('redirects to /403 when the permission is missing', async () => {
    useAuthStore.setState({ isInitialized: true, isAuthenticated: true, accessToken: 't' });
    server.use(http.get(`${API}/me/permissions`, () => HttpResponse.json(envelope([{ id: 'p', resource: 'tag', action: 'read', code: 'tag.read' }]))));
    renderAt('/users', <ProtectedRoute resource="user" action="delete"><div>secret</div></ProtectedRoute>);

    expect(await screen.findByText('forbidden page')).toBeInTheDocument();
    expect(screen.queryByText('secret')).not.toBeInTheDocument();
  });
});
