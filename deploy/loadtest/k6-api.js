// Load test for the admin API. Obtains a token from Keycloak with the
// password grant on the dev-only `load-test` client, then exercises the five
// most used read endpoints plus one write path (create/update/delete a tag).
//
// Run against a running stack:
//   docker compose --profile loadtest up k6            (uses host.docker.internal:8080)
//   k6 run -e BASE_URL=http://localhost:8080 deploy/loadtest/k6-api.js
//
// Thresholds are the SLO this project commits to for a single Postgres and
// 1–2 backend replicas; a failing threshold fails the run (exit code 99).
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const KEYCLOAK_URL = __ENV.KEYCLOAK_URL || 'http://localhost:9090';
const REALM = __ENV.REALM || 'fourati-realm';
const CLIENT_ID = __ENV.CLIENT_ID || 'load-test';
const USERNAME = __ENV.K6_USERNAME || 'superadmin';
const PASSWORD = __ENV.K6_PASSWORD || 'ChangeMe123!';

export const options = {
  scenarios: {
    steady: {
      executor: 'constant-vus',
      vus: Number(__ENV.K6_VUS || 20),
      duration: __ENV.K6_DURATION || '60s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],                   // < 1 % errors
    'http_req_duration{name:users_list}': ['p(95)<300'],
    'http_req_duration{name:me}': ['p(95)<150'],
    'http_req_duration{name:me_permissions}': ['p(95)<150'],
    'http_req_duration{name:roles_list}': ['p(95)<300'],
    'http_req_duration{name:audit_list}': ['p(95)<400'],
    'http_req_duration{name:tag_write}': ['p(95)<500'],
    rate_limited: ['rate<0.001'],                     // the per-user budget must not trip under nominal load
  },
};

const rateLimited = new Rate('rate_limited');
const tokenLatency = new Trend('token_latency');

export function setup() {
  const res = http.post(`${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`, {
    grant_type: 'password',
    client_id: CLIENT_ID,
    username: USERNAME,
    password: PASSWORD,
  });
  tokenLatency.add(res.timings.duration);
  check(res, { 'token obtained': (r) => r.status === 200 });
  if (res.status !== 200) {
    throw new Error(`Keycloak token request failed: ${res.status} ${res.body}`);
  }
  return { token: res.json('access_token') };
}

export default function (data) {
  const headers = { Authorization: `Bearer ${data.token}`, 'Content-Type': 'application/json' };
  const tag = (name) => ({ headers, tags: { name } });

  group('reads', () => {
    let r = http.get(`${BASE_URL}/api/v1/me`, tag('me'));
    check(r, { 'me 200': (x) => x.status === 200 });
    rateLimited.add(r.status === 429);

    r = http.get(`${BASE_URL}/api/v1/me/permissions`, tag('me_permissions'));
    check(r, { 'permissions 200': (x) => x.status === 200 });
    rateLimited.add(r.status === 429);

    r = http.get(`${BASE_URL}/api/v1/users?page=0&size=20&sort=createdAt,desc`, tag('users_list'));
    check(r, { 'users 200': (x) => x.status === 200, 'users paginated': (x) => x.json('pagination') != null });
    rateLimited.add(r.status === 429);

    r = http.get(`${BASE_URL}/api/v1/roles?page=0&size=20`, tag('roles_list'));
    check(r, { 'roles 200': (x) => x.status === 200 });
    rateLimited.add(r.status === 429);

    r = http.get(`${BASE_URL}/api/v1/audit-logs?page=0&size=20`, tag('audit_list'));
    check(r, { 'audit 200': (x) => x.status === 200 });
    rateLimited.add(r.status === 429);
  });

  group('write', () => {
    const name = `k6-${__VU}-${__ITER}-${Date.now()}`;
    const created = http.post(`${BASE_URL}/api/v1/tags`, JSON.stringify({ name, color: '#336699' }), tag('tag_write'));
    check(created, { 'tag created': (x) => x.status === 201 });
    rateLimited.add(created.status === 429);
    if (created.status === 201) {
      const id = created.json('data.id');
      const version = created.json('data.version');
      const updated = http.put(`${BASE_URL}/api/v1/tags/${id}`,
        JSON.stringify({ name: name + '-u', color: '#336699', version }), tag('tag_write'));
      check(updated, { 'tag updated': (x) => x.status === 200 });
      const deleted = http.del(`${BASE_URL}/api/v1/tags/${id}`, null, tag('tag_write'));
      check(deleted, { 'tag deleted': (x) => x.status === 204 });
    }
  });

  sleep(1);
}
