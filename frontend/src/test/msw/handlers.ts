import { http, HttpResponse } from 'msw';

/**
 * MSW handlers shaped exactly like the backend's contract: the ApiResponse
 * envelope for 2xx and RFC 9457 problem details for errors, so tests exercise
 * the real unwrap/unwrapPage/problemOf code paths. Extend per test with
 * `server.use(...)` for error cases.
 */
export const API = 'http://localhost:8080/api/v1';

export function envelope<T>(data: T, extra: Record<string, unknown> = {}) {
  return {
    success: true,
    status: 200,
    message: 'ok',
    timestamp: new Date().toISOString(),
    correlationId: 'test-correlation-id',
    data,
    ...extra,
  };
}

/** An error exactly as the backend's `Problems` factory writes it (application/problem+json). */
export function problem(status: number, code: string, detail: string, violations?: Array<{ field: string; code?: string; message: string }>) {
  return HttpResponse.json(
    {
      type: `https://errors.example.com/${code.toLowerCase().replace(/_/g, '-')}`,
      title: code.replace(/_/g, ' ').toLowerCase(),
      status,
      detail,
      instance: '/api/v1/x',
      code,
      retryable: false,
      timestamp: new Date().toISOString(),
      correlationId: 'test-correlation-id',
      ...(violations ? { violations: violations.map((v) => ({ code: 'INVALID', ...v })) } : {}),
    },
    { status, headers: { 'Content-Type': 'application/problem+json' } },
  );
}

/** @deprecated use {@link problem}; kept so existing tests keep compiling. */
export const errorEnvelope = problem;

export const sampleUser = {
  id: '11111111-1111-1111-1111-111111111111',
  username: 'alice',
  email: 'alice@example.com',
  firstName: 'Alice',
  lastName: 'Admin',
  status: 'active',
  superuser: false,
  createdAt: '2026-09-01T00:00:00Z',
  updatedAt: '2026-09-01T00:00:00Z',
  version: 3,
};

export const handlers = [
  http.get(`${API}/me`, () => HttpResponse.json(envelope(sampleUser))),

  http.get(`${API}/me/permissions`, () =>
    HttpResponse.json(envelope([
      { id: 'p1', resource: 'user', action: 'read', code: 'user.read' },
      { id: 'p2', resource: 'user', action: 'edit', code: 'user.edit' },
    ])),
  ),

  http.get(`${API}/users`, ({ request }) => {
    const url = new URL(request.url);
    const q = url.searchParams.get('q');
    const items = q && !sampleUser.username.includes(q) ? [] : [sampleUser];
    return HttpResponse.json(envelope(items, {
      pagination: { page: 0, size: 20, totalElements: items.length, totalPages: 1, first: true, last: true },
    }));
  }),

  http.post(`${API}/users`, async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    return HttpResponse.json(envelope({ ...sampleUser, ...body, id: '22222222-2222-2222-2222-222222222222', version: 0 }), { status: 201 });
  }),

  http.put(`${API}/users/:id`, async ({ request, params }) => {
    const body = (await request.json()) as Record<string, unknown>;
    if (body.version !== undefined && body.version !== sampleUser.version) {
      return errorEnvelope(409, 'CONCURRENT_MODIFICATION', 'The resource was modified by someone else. Reload and retry.');
    }
    return HttpResponse.json(envelope({ ...sampleUser, ...body, id: params.id, version: sampleUser.version + 1 }));
  }),
];
