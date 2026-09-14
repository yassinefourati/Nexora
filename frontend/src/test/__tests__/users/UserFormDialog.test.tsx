import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/msw/server';
import { API, envelope, sampleUser } from '@/test/msw/handlers';
import { createQueryWrapper } from '@/test/utils/queryWrapper';

vi.mock('@/core/auth/oidcUserManager', () => ({
  oidcUserManager: {
    signinSilent: vi.fn(), signinRedirect: vi.fn(), signoutRedirect: vi.fn(), stopSilentRenew: vi.fn(),
    getUser: vi.fn().mockResolvedValue(null), removeUser: vi.fn(),
    events: { addUserLoaded: vi.fn(), addUserUnloaded: vi.fn(), addSilentRenewError: vi.fn() },
  },
}));

const { default: apiClient } = await import('@/core/api/client');
const { default: UserFormDialog } = await import('@/features/users/components/UserFormDialog');

function renderDialog(props: Partial<React.ComponentProps<typeof UserFormDialog>> = {}) {
  const Wrapper = createQueryWrapper();
  const onClose = vi.fn();
  render(<Wrapper><UserFormDialog open onClose={onClose} {...props} /></Wrapper>);
  return { onClose };
}

describe('UserFormDialog', () => {
  beforeAll(() => { server.listen({ onUnhandledRequest: 'error' }); apiClient.defaults.baseURL = API; });
  afterEach(() => server.resetHandlers());
  afterAll(() => server.close());

  it('shows validation messages and does not submit an invalid create form', async () => {
    const user = userEvent.setup();
    let posted = false;
    server.use(http.post(`${API}/users`, () => { posted = true; return HttpResponse.json(envelope(sampleUser), { status: 201 }); }));
    const { onClose } = renderDialog();

    await user.type(screen.getByLabelText(/email/i), 'not-an-email');
    await user.click(screen.getByRole('button', { name: /create/i }));

    expect(await screen.findAllByText(/invalid|too small|required|at least/i)).not.toHaveLength(0);
    expect(posted).toBe(false);
    expect(onClose).not.toHaveBeenCalled();
  });

  it('creates a user without a password (Keycloak owns sign-in) and closes', async () => {
    const user = userEvent.setup();
    const captured: { body?: Record<string, unknown> } = {};
    server.use(http.post(`${API}/users`, async ({ request }) => {
      captured.body = (await request.json()) as Record<string, unknown>;
      return HttpResponse.json(envelope({ ...sampleUser, ...captured.body }), { status: 201 });
    }));
    const { onClose } = renderDialog();

    await user.type(screen.getByLabelText(/username/i), 'bob');
    await user.type(screen.getByLabelText(/first name/i), 'Bob');
    await user.type(screen.getByLabelText(/last name/i), 'Builder');
    await user.type(screen.getByLabelText(/^email/i), 'bob@example.com');
    await user.click(screen.getByRole('button', { name: /create/i }));

    await vi.waitFor(() => expect(onClose).toHaveBeenCalled());
    expect(captured.body).toMatchObject({ username: 'bob', email: 'bob@example.com', firstName: 'Bob', lastName: 'Builder' });
    expect(captured.body?.password).toBeUndefined();
  });

  it('sends the version it was given when editing, so a stale edit is refused with 409', async () => {
    const user = userEvent.setup();
    const captured: { body?: Record<string, unknown> } = {};
    server.use(http.put(`${API}/users/${sampleUser.id}`, async ({ request }) => {
      captured.body = (await request.json()) as Record<string, unknown>;
      return HttpResponse.json(envelope({ ...sampleUser, ...captured.body, version: sampleUser.version + 1 }));
    }));
    const { onClose } = renderDialog({ editUser: sampleUser });

    const first = screen.getByLabelText(/first name/i);
    await user.clear(first);
    await user.type(first, 'Alicia');
    await user.click(screen.getByRole('button', { name: /save/i }));

    await vi.waitFor(() => expect(onClose).toHaveBeenCalled());
    expect(captured.body).toMatchObject({ firstName: 'Alicia', version: sampleUser.version });
  });
});
