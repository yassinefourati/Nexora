# Admin API → Frontend Migration Matrix

Source of truth for this migration. Backend: `backend/` (Spring Boot 4, JWT resource server, `ApiConstants.VERSION = "/api/v1"`). Frontend: `frontend/` (React 19, Vite, MUI, TanStack Query, Zustand, RHF+Zod).

## Critical findings (read before implementing anything)

1. **Auth model mismatch (highest risk).** Backend is a stateless OAuth2/JWT resource server (`SecurityConfig.java`) with **no login endpoint of its own** — it validates externally-issued JWTs, `issuer-uri: http://localhost:9090/realms/fourati-realm` (Keycloak-shaped). Frontend's entire auth system (`/auth/login`, `/auth/refresh`, `/csrf-token`, BFF cookie plan) assumes a backend that issues its own tokens. **Decision: stand up Keycloak locally matching the issuer-uri, implement real OIDC Authorization Code + PKCE, remove the fictional login/refresh/CSRF/BFF code.**
2. **Response envelope mismatch.** Real backend: every response wrapped in `ApiResponse<T>` = `{ success, status, message, timestamp, correlationId, data, pagination?, error? }`. Pagination is zero-based (`page: 0` = first), fields `page/size/totalElements/totalPages/first/last`. Frontend currently has **three mutually inconsistent invented envelopes** across `usersApi.ts` (one-based `meta{page,limit,total,totalPages}`), MSW `authHandlers.ts` (`status:'success'` string vs `code:number`), and stale contract tests (no envelope at all). **Decision: rewrite to match the real backend's envelope exactly, one unwrapper, used everywhere.**
3. **Pagination is inconsistent on the backend itself** — many list endpoints return unpaginated `ApiResponse.ok` (no `pagination` block) when certain filter query params are supplied, and paginated otherwise (see per-controller notes below). Frontend hooks must check for `pagination` presence, never assume.
4. **Auth/authorization per-endpoint is inconsistent by design** — some controllers are fully open to any authenticated user (Department, Organization, OrganizationMember, Team, UserIdentityProvider — zero `@PreAuthorize`), some are fully `ROLE_ADMIN`-gated at class level, others mixed per-method. RBAC guards must be wired per-endpoint from the table below, not assumed per-resource.
5. **`openapi.yaml` in the frontend is stale/fictional** (2 paths, integer ids, doesn't match anything). Backend serves live OpenAPI at `/v3/api-docs` (public path) — regenerate frontend types from that, not the checked-in stub.
6. **RBAC is currently static** in the frontend (`shared/types/roles.ts`: 3 hardcoded roles × 7 hardcoded resources). Backend has full dynamic RBAC (`Role`, `Permission`, `RolePermission`, `UserRole`, plus menu-level `Menu`/`MenuItem`/`MenuPermission`/`RoleMenu`). Must move to data-driven permission checks.
7. **~20 backend entities have zero frontend coverage today.** ~6 existing frontend features (dashboard, analytics, reports, invite, 2FA-in-profile) are speculative/fictional with **no backend counterpart at all** — flag as gaps, do not build against them further; either drop or explicitly scope as net-new backend work.
8. Reusable foundation worth keeping: `AdvancedDataTable` (client-side table primitive — needs a server-driven pagination/sort variant), `Can`/`usePermission` (needs to become data-driven), `ConfirmDialog`, `GlobalFeedback` (toast/snackbar), skeleton components, `PageErrorBoundary`/`SectionErrorBoundary`, lazy-route pattern in `AppRoutes.tsx`, `RowActionsMenu`.

## Backend controller → frontend module matrix

Legend: **Auth** column shows the effective `@PreAuthorize` requirement per verb group (R=read endpoints, W=write/mutate endpoints) — `open`=any authenticated user, `ADMIN`=`hasRole('ADMIN')`.

| # | Backend controller | Base path | Auth (R / W) | Special notes | Frontend status | Phase |
|---|---|---|---|---|---|---|
| 1 | UserController | `/users` | open / ADMIN | soft-delete | Exists, wrong path/shape/envelope — rework | 3 |
| 2 | RoleController | `/roles` | ADMIN / ADMIN | — | Missing (static matrix stands in) | 3 |
| 3 | PermissionController | `/permissions` | ADMIN / ADMIN | `code` generated `resource.action` | Missing | 3 |
| 4 | RolePermissionController | `/role-permissions` | ADMIN / ADMIN | no update endpoint; `by-role/{id}`, `by-permission/{id}` | Missing | 3 |
| 5 | UserRoleController | `/user-roles` | mixed | `by-user/{id}`, `by-role/{id}` | Missing | 3 |
| 6 | OrganizationController | `/organizations` | open / open | soft-delete; `/{id}/children` | Missing | 3 |
| 7 | OrganizationMemberController | `/organization-members` | open / open | filters: org/user/dept/team (unpaged when filtered) | Missing | 3 |
| 8 | DepartmentController | `/departments` | open / open | soft-delete; filters org/parent (unpaged when filtered) | Missing | 3 |
| 9 | TeamController | `/teams` | open / open | soft-delete; filters org/dept (unpaged when filtered) | Missing | 3 |
| 10 | AppModuleController | `/app-modules` | open / ADMIN | `/active` unpaged list | Missing | 4 |
| 11 | MenuController | `/menus` | open / ADMIN | — | Missing (static `menuConfig.tsx` stands in) | 4 |
| 12 | MenuItemController | `/menu-items` | open / ADMIN | filters menuId/parentMenuItemId/rootOnly (unpaged when filtered) | Missing | 4 |
| 13 | MenuPermissionController | `/menu-permissions` | ADMIN / ADMIN | filters (unpaged when filtered) | Missing | 4 |
| 14 | RoleMenuController | `/role-menus` | ADMIN / ADMIN | filters (unpaged when filtered) | Missing | 4 |
| 15 | SettingController | `/settings` | open / ADMIN | `by-scope/{scope}`, `by-scope/{scope}/organization/{orgId}`; `SETTING_NOT_EDITABLE` error | Partial (notification-settings only, wrong shape) | 4 |
| 16 | FeatureFlagController | `/feature-flags` | open / ADMIN | `by-organization/{id}` unpaged | Missing (env-var flags stand in — different concept, keep both) | 4 |
| 17 | MetadataKvController | `/metadata` | open / open | polymorphic; `/lookup?entityType&entityId&key` | Missing | 4 |
| 18 | AuditLogController | `/audit-logs` | ADMIN / — (read-only) | unpaged when `entityType`+`entityId` both given | Exists as `/audit`, wrong path/shape | 5 |
| 19 | AuthLogController | `/auth-logs` | ADMIN / — (read-only) | filters userId/eventType | Missing | 5 |
| 20 | LoginHistoryController | `/login-history` | open(POST/PATCH) / ADMIN(GET) | PATCH closes session (logout) | Missing (profile/sessions is a different, partial concept) | 5 |
| 21 | SessionController | `/sessions` | ADMIN(list) / open(else) | `/revoke` action | Partial (`profile/sessions`, wrong path, self-only) | 5 |
| 22 | ApiKeyController | `/api-keys` | ADMIN / ADMIN | secret returned once on create; `/revoke` not delete | Missing | 5 |
| 23 | ErrorLogController | `/error-logs` | ADMIN / — (read-only) | filters severity/source | Missing | 5 |
| 24 | ErrorCatalogController | `/errors` | open / — (dev-only) | **absent in prod** (`@Profile("!prod")`); `/simulate/{status}` dev tool | Missing | 5 |
| 25 | SystemEventController | `/system-events` | ADMIN / — (read-only) | filters eventType/severity | Missing (SystemHealth page is a different concept — Actuator) | 5 |
| 26 | NotificationController | `/notifications` | open(most) / ADMIN(delete) | — | Partial, flattens 3 backend resources into 1 | 6 |
| 27 | NotificationTemplateController | `/notification-templates` | open / ADMIN | — | Missing | 6 |
| 28 | UserNotificationController | `/user-notifications` | open / open | `/unread-count`, `/{id}/read` | Partial (flattened into notifications feature) | 6 |
| 29 | TagController | `/tags` | open / open | — | Missing | 6 |
| 30 | EntityTagController | `/entity-tags` | open / open | polymorphic; bulk delete by entity | Missing | 6 |
| 31 | CommentController | `/comments` | open / open | threaded via `parentCommentId`; `userId` passed as query param on create | Missing | 6 |
| 32 | AttachmentController | `/attachments` | open / open | no update; `by-uploader/{id}` | Missing | 6 |
| 33 | UserIdentityProviderController | `/user-identity-providers` | open / open | link/unlink SSO, no update | Missing | 3 (with Users) |

**No backend equivalent exists for:** dashboard stats/activity, analytics (overview/retention/geography/funnel), reports (scheduled exports), invite, 2FA/TOTP setup. These are documented as backend gaps in the final report (§ Phase 8), not implemented against fake endpoints.

## DTO reference

See full field-by-field DTO inventory captured during discovery (all `Create*Request`/`Update*Request`/`*Response` records, exact field names/types/validation) — used directly when generating `src/shared/types/api.generated.ts` from the live `/v3/api-docs` spec in Phase 2d. Key conventions: `UUID` → `string`, `Instant` → ISO `string`, jsonb-ish fields (`metadata`, `value`, `data`, `payload`, `context`, `beforeData`/`afterData`, `conditions`, `rawProfile`) are `string` (raw JSON text, not parsed objects), password never appears in any response.

## Phase plan

- **Phase 2 (Foundation):** Keycloak + OIDC auth, API client envelope rewrite, generated types from live OpenAPI, generic `useCrudResource` hook, server-driven DataTable variant, data-driven `Can`/`usePermission`.
- **Phase 3 (Core Admin):** Users (rework), Roles, Permissions, RolePermissions, UserRoles, Organizations, OrganizationMembers, Departments, Teams, UserIdentityProviders.
- **Phase 4 (App Config):** AppModules, Menus, MenuItems, MenuPermissions, RoleMenus, Settings (rework), FeatureFlags (real, DB-backed), MetadataKv.
- **Phase 5 (Security/Audit):** AuditLogs (rework), AuthLogs, LoginHistory, Sessions (rework to admin-wide), ApiKeys, ErrorLogs, ErrorCatalog, SystemEvents.
- **Phase 6 (Notifications/Content):** Notifications + NotificationTemplates + UserNotifications (unflatten into 3 proper resources), Tags, EntityTags, Comments, Attachments.
- **Phase 7 (Dashboard):** rebuild using only real endpoints (composed client-side from Users/Sessions/AuditLogs/etc. counts — no fictional aggregation endpoint exists).
- **Phase 8 (Hardening):** contract verification against live backend, permission verification, tests, build verification, final migration report per the required format (Completed / Partially Completed / Backend Gaps / Frontend Gaps / API Contract Issues / Permission Issues / Tests / Remaining Work).

Status of each phase is tracked in the session's todo list, not duplicated here.
