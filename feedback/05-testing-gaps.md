# Testing gaps

## Backend

The existing 63 tests (now 69 after adding `OwnershipSecurityTest`) are
almost entirely unit tests with mocked repositories, plus 2 real
Testcontainers integration tests (`UserCrudIntegrationTest`,
`ApiKeyAuditIntegrationTest`) and the ArchUnit architecture suite.

**What's missing that matters most: HTTP-level authorization tests.**
Nothing spins up the actual security filter chain and asserts "a JWT with
role X gets status Y on endpoint Z." `SecurityConfigTest` only unit-tests
the JWT-to-authorities converter in isolation — it never exercises a real
`@PreAuthorize` check end to end. So the `@ownership.*` expressions I added
are unit-tested (mocked repository, direct method call) but not verified
against an actual HTTP request through Spring Security's method security.

If you want confidence that the authorization matrix is actually correct —
not just that the Java expressions compile — add a
`@SpringBootTest(webEnvironment = RANDOM_PORT)` (or `@WebMvcTest` per
controller) test class that:

1. Builds JWTs for a few personas (admin, "alice" who owns some rows,
   "bob" who doesn't, anonymous)
2. Hits every mutating endpoint with each persona
3. Asserts against a checked-in table of expected status codes

This would have caught the original IDOR bugs automatically rather than
requiring a manual review pass, and it locks the authorization contract in
place so a future change can't silently regress it.

**Other backend gaps:**
- No test exercises `RateLimitFilter` or `IdempotencyFilter` directly (e.g.
  a test asserting that a second request with the same idempotency key from
  a *different* principal does NOT get the cached response — this is
  exactly the bug I fixed, and there's no regression test pinning it down).
- `AuditLogEventListener`'s new `@TransactionalEventListener(AFTER_COMMIT)`
  behavior isn't tested — the existing `AuditLogEventListenerTest` calls
  `listener.onAuditEvent(event)` directly, which bypasses the transaction
  phase entirely and would pass even if the annotation were removed.
- `UserNotificationService`'s after-commit WebSocket push has the same
  problem — no test verifies the push actually waits for commit.
- Only 2 of the 33 resources have a real Testcontainers integration test
  (Users, ApiKey+Audit). The rest are unit-tested with mocks only, which
  means a Flyway migration mismatch or a real Postgres constraint violation
  wouldn't be caught until manual testing.

## Frontend

9 unit tests (`src/test/__tests__/`), 4 Playwright specs. For an app with
~30 feature modules and complex auth/permission logic, this is thin.

**Highest-value additions, in order:**

1. **The 401 silent-renew race in `client.ts`** — this is genuinely tricky
   logic (only retry once, only if the original request carried a token,
   coalesce concurrent renewal attempts into one `signinSilent()` call). It
   has no test at all. A bug here either loops forever or logs users out
   spuriously; worth pinning down with a test that mocks two concurrent 401s
   and asserts `signinSilent()` is called exactly once.

2. **`ProtectedRoute` permission redirect** — no test asserts that a user
   without the required permission actually lands on `/403`, or that an
   unauthenticated user lands on `/login`.

3. **`useCrudResource` error paths** — the happy path presumably works (it's
   used everywhere), but there's no test for what happens when the backend
   returns a validation error, a 409 conflict, or a network failure.

4. **Form dialogs** (`UserFormDialog`, `SettingFormDialog`, etc.) — zero
   tests on the actual create/edit forms, which is where most user-facing
   bugs in an admin panel tend to live (validation messages, required-field
   handling, submit-disabled states).

MSW is installed (`msw` in devDependencies, worker file checked in) but I
found no handler files — worth generating a base set from the OpenAPI spec
(`npm run generate-types` already pulls the schema; a matching MSW handler
generator would keep them in sync) rather than hand-writing 30+ handlers.

## E2E

The 4 Playwright specs cover login, navigation, users, and accessibility.
Reasonable starting set. Missing: a smoke test that runs against the actual
`docker compose up` stack in CI (currently E2E only runs against
`npm run dev`, which uses MSW mocks, not the real backend — so it never
catches a genuine frontend/backend contract mismatch).
