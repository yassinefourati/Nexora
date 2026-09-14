import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { server } from '@/test/msw/server';
import { API, envelope, errorEnvelope } from '@/test/msw/handlers';
import { createQueryWrapper } from '@/test/utils/queryWrapper';
import type { ListParams, ResourcePage } from '@/core/api/useCrudResource';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';

vi.mock('@/core/auth/oidcUserManager', () => ({
  oidcUserManager: {
    signinSilent: vi.fn(), signinRedirect: vi.fn(), signoutRedirect: vi.fn(), stopSilentRenew: vi.fn(),
    getUser: vi.fn().mockResolvedValue(null), removeUser: vi.fn(),
    events: { addUserLoaded: vi.fn(), addUserUnloaded: vi.fn(), addSilentRenewError: vi.fn() },
  },
}));

const { default: apiClient } = await import('@/core/api/client');
const { useAuthStore } = await import('@/core/auth/stores/useAuthStore');
const { useConfirmStore } = await import('@/shared/stores/useConfirmStore');
const { default: CrudPage } = await import('@/shared/components/crud/CrudPage');

interface Widget { id?: string; name?: string; version?: number }

const widgets: Widget[] = [{ id: 'w1', name: 'Alpha', version: 0 }, { id: 'w2', name: 'Beta', version: 0 }];
const requests: string[] = [];

function useWidgets(params: ListParams) {
  return useQuery({
    queryKey: ['widgets', params],
    queryFn: () => apiClient.get(`${API}/widgets`, { params }).then((r): ResourcePage<Widget> => ({ items: r.data.data, pagination: r.data.pagination })),
  });
}
function useRemoveWidget() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiClient.delete(`${API}/widgets/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['widgets'] }),
  });
}

const columns: ColumnDef<Widget>[] = [{ field: 'name', headerName: 'Name', width: 200 }];

function Page(props: Partial<React.ComponentProps<typeof CrudPage<Widget>>> = {}) {
  return (
    <CrudPage<Widget>
      resource="tags"
      useList={useWidgets}
      useRemove={useRemoveWidget}
      columns={columns}
      rowLabel={(row) => row.name ?? ''}
      renderDialog={({ open, editItem }) => open ? <div role="dialog">{editItem ? `edit ${editItem.name}` : 'create'}</div> : null}
      {...props}
    />
  );
}

function renderPage(ui: ReactNode = <Page />) {
  const Wrapper = createQueryWrapper();
  return render(<Wrapper>{ui}</Wrapper>);
}

describe('CrudPage', () => {
  beforeAll(() => {
    server.listen({ onUnhandledRequest: 'error' });
    apiClient.defaults.baseURL = API;
    useAuthStore.setState({ isAuthenticated: true, isInitialized: true, accessToken: 't' });
  });
  afterEach(() => { server.resetHandlers(); requests.length = 0; });
  afterAll(() => server.close());

  const listHandler = (items: Widget[] = widgets) => http.get(`${API}/widgets`, ({ request }) => {
    requests.push(new URL(request.url).search);
    return HttpResponse.json(envelope(items, { pagination: { page: 0, size: 20, totalElements: items.length, totalPages: 1, first: true, last: true } }));
  });

  it('renders the translated title, description and one row per item, with an actions column', async () => {
    server.use(listHandler());
    renderPage();

    expect(await screen.findByRole('heading', { level: 1, name: 'Tags' })).toBeInTheDocument();
    expect(screen.getByText('Labels that can be attached to any entity.')).toBeInTheDocument();
    expect(await screen.findByRole('cell', { name: 'Alpha' })).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: /more actions/i })).toHaveLength(2);
  });

  it('sends search as ?q= to the server instead of filtering the page', async () => {
    server.use(listHandler());
    const user = userEvent.setup();
    renderPage();
    await screen.findByRole('cell', { name: 'Alpha' });

    await user.type(screen.getByLabelText('Search'), 'alp{Enter}');

    await vi.waitFor(() => expect(requests.some((q) => q.includes('q=alp'))).toBe(true));
  });

  it('shows the error state with a retry button when the list fails', async () => {
    server.use(http.get(`${API}/widgets`, () => errorEnvelope(500, 'INTERNAL_ERROR', 'boom')));
    renderPage();

    expect(await screen.findByText('Failed to load data from the server.')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument();
  });

  it('shows the empty state with a create shortcut when there are no rows', async () => {
    server.use(listHandler([]));
    const user = userEvent.setup();
    renderPage();

    expect(await screen.findByText(/No Tags yet/i)).toBeInTheDocument();
    await user.click(screen.getAllByRole('button', { name: 'Add Tag' })[0]);
    expect(screen.getByRole('dialog')).toHaveTextContent('create');
  });

  it('hides create, edit and delete when the caller lacks the permission', async () => {
    server.use(listHandler(), http.get(`${API}/me/permissions`, () => HttpResponse.json(envelope([{ id: 'p', resource: 'role', action: 'read', code: 'role.read' }]))));
    renderPage(<Page permission="role" />);

    await screen.findByRole('cell', { name: 'Alpha' });
    expect(screen.queryByRole('button', { name: 'Add Tag' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /more actions/i })).not.toBeInTheDocument();
  });

  it('confirms before deleting and calls the API with the row id', async () => {
    let deleted = '';
    server.use(listHandler(), http.delete(`${API}/widgets/:id`, ({ params }) => { deleted = String(params.id); return new HttpResponse(null, { status: 204 }); }));
    const user = userEvent.setup();
    renderPage();
    const row = (await screen.findByRole('cell', { name: 'Beta' })).closest('tr')!;

    await user.click(within(row).getByRole('button', { name: /more actions/i }));
    await user.click(screen.getByRole('menuitem', { name: 'Delete' }));

    const confirm = useConfirmStore.getState();
    expect(confirm.open).toBe(true);
    expect(confirm.options?.message).toContain('Beta');
    confirm.options?.onConfirm();
    await vi.waitFor(() => expect(deleted).toBe('w2'));
  });

  it('opens the dialog in edit mode from the row menu', async () => {
    server.use(listHandler());
    const user = userEvent.setup();
    renderPage();
    const row = (await screen.findByRole('cell', { name: 'Alpha' })).closest('tr')!;

    await user.click(within(row).getByRole('button', { name: /more actions/i }));
    await user.click(screen.getByRole('menuitem', { name: 'Edit' }));

    expect(screen.getByRole('dialog')).toHaveTextContent('edit Alpha');
  });
});
