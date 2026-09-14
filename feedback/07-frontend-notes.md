# Frontend notes

## Server-side search doesn't exist

`Users.tsx` filters client-side over only the *currently loaded page* of
results (`useMemo` filtering `data?.items`), with a comment acknowledging
this: "the real backend has no text-search query param on GET /users —
filter client-side over the current page only, as a convenience, not a
substitute for server-side search." Searching for a user on page 2 while
viewing page 1 finds nothing, silently. This is a real bug from a user's
perspective (search that doesn't search), not just a limitation.

`UserRepository` already extends `JpaSpecificationExecutor<User>` and
nothing uses it. Adding a `?q=` param backed by a `Specification` is
backend work (a couple hours) that would let the frontend do this properly.
Worth doing for any list page where users search for something specific by
name, not just Users.

## Pagination inconsistency between filtered and unfiltered list endpoints

`MIGRATION.md` documents this itself: several endpoints return an unpaged
full list when certain filter query params are supplied, and a paginated
`Page<T>` otherwise (e.g. Department/Team/OrganizationMember "unpaged when
filtered"). The frontend's `unwrapPage()` helper throws if the `pagination`
block is missing. So today, any frontend code path that calls a filtered
list endpoint has to know in advance whether to use `unwrap` or `unwrapPage`
— it's not a bug per se (the frontend appears to be handling it correctly
per-hook), but it's a persistent trap for anyone adding a new list view: get
it wrong and you get a runtime throw, not a type error, since the OpenAPI
types don't distinguish "paginated" from "unpaginated" responses for the
same endpoint. Worth picking one behavior (always paginate, with a large
default page size for filtered "give me everything" cases) rather than two.

## i18n is decorative

`en.ts` has content, `fr.ts` has roughly 5 strings translated. If French
localization isn't actually a near-term goal, it might be worth removing
`react-i18next`/`i18next` entirely rather than keeping an unfinished
localization layer — half-translated UI is a worse experience than
untranslated UI with no i18n framework at all. If it *is* a goal, it needs
real investment, not something I'd want to guess at translating myself.

## Sentry user typing bug

`core/config/sentry.ts`'s `setSentryUser` takes `{ id: number; email: string;
role: string }`, but every id in this app is a UUID string
(`AuthUser.sub: string`). I found no caller of `setSentryUser` anywhere in
the codebase — it may be entirely unused, in which case fix the type when
you wire it up, or delete it if Sentry user context isn't planned.

## Bundle size: doc pages ship in production

Covered in `01-dead-code-to-delete.md` but worth repeating here since it's
frontend-specific: `UiDocsPage.tsx` (1204 lines) and `DevDocsPage.tsx` (847
lines) are lazy-loaded routes, so they don't bloat the *initial* bundle, but
they do ship to production and are reachable by anyone who knows/guesses the
URL. If these are meant for internal developer reference only, either gate
them behind a `import.meta.env.DEV` check in the router, or move them to
Storybook/a separate docs site that isn't part of the deployed app.

## WebSocket reconnect has no backoff cap

`useWebSocketStore.ts` sets `reconnectDelay: 5000` (a fixed 5-second retry).
If the backend is down for an extended period, every connected client
hammers it every 5 seconds indefinitely with no exponential backoff or
jitter. Not urgent at current scale, but worth knowing about if this ever
needs to handle a real outage gracefully rather than adding to the load
during backend recovery (a thundering-herd-on-reconnect pattern).
