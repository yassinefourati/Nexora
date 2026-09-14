# ADR 0005: RFC 9457 problem details for every error; the success envelope stays in v1

Status: accepted (2026-09-13)

## Context

The API returns three different error shapes:

- Exceptions handled by `GlobalExceptionHandler` return the custom
  `ApiResponse` envelope with `success: false` and a nested `error` object.
  The content type is already `application/problem+json`, but the body is
  not a problem-details document.
- `ApiKeyAuthenticationFilter` writes a hand-built RFC 9457 body for an
  invalid key.
- The bearer-token entry point and the access-denied handler use Spring
  Security defaults: an empty body with a `WWW-Authenticate` header.

Spring Framework 6 and Spring Boot 4 ship `ProblemDetail` and
`ErrorResponseException` natively. Clients and tooling expect one contract.

## Decision

- Every error response is an RFC 9457 problem-details document with media
  type `application/problem+json`. Standard members: `type`, `title`,
  `status`, `detail`, `instance`. Extension members: `code` (stable
  `ErrorCode`), `correlationId`, `retryable`, and `violations[]` for
  validation failures with `field`, `code`, `message`, `rejectedValue`.
- `type` is `<error.base-url>/<code-in-kebab-case>`, unchanged from today.
- The same shape is produced by the exception handler, the bearer
  authentication entry point, the access-denied handler, the API-key filter,
  the rate-limit filter (429) and the idempotency filter.
- Successful responses keep the `ApiResponse` envelope in `/api/v1`. The
  frontend unwraps it in one place and nothing is gained by breaking every
  read at the same time as the error contract. Unwrapping is deferred to a
  `/api/v2` prefix if it is ever wanted.
- The error catalog endpoint is generated from the `ErrorCode` enum rather
  than a hand-maintained list, so a new code cannot be forgotten.
- The contract is covered by a controller-slice test per error family and a
  full-chain test for the security entry points.

## Consequences

- One shape for clients to parse. The frontend keys its retry and redirect
  logic on HTTP status, so the change is contained to its error parsing.
- `ApiResponse.error(...)`, `ApiResponse.ErrorDetail` and the
  `success: false` branch are removed once every producer is migrated.
- Rejected alternative: keep the envelope for errors and only fix the two
  outliers. It keeps a bespoke shape that no library understands and leaves
  the `success` flag redundant with the HTTP status.
