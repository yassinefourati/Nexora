# Dead code to delete

I flagged these in the review and started removing them last session, but the
delete was blocked by the auto-mode permission classifier ("irreversible
local destruction"). Nothing here is used anywhere in the codebase — I
grepped for callers before listing them.

Run this yourself when ready:

```bash
git rm -r frontend/bff
git rm backend/common/src/main/java/com/fourati/platform/util/EncryptionUtils.java
git rm backend/common/src/main/java/com/fourati/platform/util/RetryUtils.java
git rm backend/common/src/main/java/com/fourati/platform/util/SanitizationUtils.java
git rm backend/common/src/main/java/com/fourati/platform/scheduler/ScheduledJobTemplate.java
```

## Why each one

- **`frontend/bff/`** — a 20-line Express "hello world" that MIGRATION.md and
  its own README describe as a future BFF for httpOnly-cookie auth. The
  actual frontend uses PKCE + Bearer tokens directly (`oidcUserManager.ts`),
  which doesn't need a BFF. Either build it for real or delete it — a stub
  that does nothing reads as unfinished, not as a plan.

- **`EncryptionUtils.java`** — AES-256-GCM helper, well-written, zero callers.
  Nothing in the app encrypts anything at rest. Keep it only if you have a
  concrete near-term use (e.g. encrypting a field before storing it);
  otherwise it's just surface area to maintain.

- **`RetryUtils.java`** — its own javadoc says "for production-grade retry
  consider Spring Retry or Resilience4j." Zero callers. If you want retries
  on the Keycloak JWKS fetch or an outbound call, use Resilience4j instead of
  reviving this.

- **`SanitizationUtils.java`** — zero callers. `@SafeInput` validation
  (used 44 times) already does the actual input rejection at the DTO level;
  this class duplicates part of that surface without being wired in anywhere.

- **`ScheduledJobTemplate.java`** — a template method class for building
  `@Scheduled` jobs with structured logging. Zero callers; the two real
  `@Scheduled` methods that exist (`RateLimitFilter.evictStaleBuckets`,
  `IdempotencyFilter.evictExpired`) don't use it.

## Also worth a look, not deleted (used, but marginal)

- **`ExportService`** (`common/platform/export/`) — CSV/PDF export
  infrastructure, one reference in the codebase but no controller actually
  calls it to export anything (checked: no `@GetMapping` returns
  `text/csv` or a PDF anywhere in `presentation/api/`). Either wire it onto
  something like `GET /audit-logs/export` or drop it. Given `AuditLog` is the
  obvious export candidate, wiring it in is probably less work than deleting
  it.

- **`backend/.env.example`** — still says "template" in a comment,
  mentions Redis and an `APP_RATE_LIMIT_RPM` variable that doesn't match the
  `fourati.common.rate-limit.rpm` property actually used. This file predates
  the current app; rewrite it to match reality or delete it (the root
  `.env.example` already covers the real Docker variables).

- **`SqlInjectionFilter`** (`common/platform/security/`) — fully implemented,
  disabled by default (`fourati.common.sql-injection.enabled: false`), with a
  comment explaining it false-positives on legitimate admin text. It's not
  dead code exactly, but it's inert. Decide: delete it (the app is 100%
  parameterized JPA, so it adds no real protection), or flip it to
  `LOG_ONLY` in prod for a monitoring signal. Don't leave it as dead weight
  people might assume is protecting something.

- **`UiDocsPage.tsx`** (1204 lines) and **`DevDocsPage.tsx`** (847 lines) in
  the frontend — developer documentation pages shipped as lazy routes in the
  production bundle. Consider Storybook instead, or gate them behind a dev
  build flag so they don't inflate the shipped bundle.
