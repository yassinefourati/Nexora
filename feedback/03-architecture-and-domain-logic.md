# Architecture: the CRUD-multiplication problem

This is the biggest structural thing I noticed and the one I did the least
about, because it's a real redesign, not a bug fix.

## What's there today

33 tables, 33 controllers, 33 services, 33 MapStruct mappers, 34
repositories — almost all following the same shape:
`create / findById / findAll / update / delete`, each a thin pass-through
from controller to service to repository. ~17,000 lines of Java, and most of
it is this pattern repeated with different type names.

This isn't wrong, exactly — reference/lookup tables (`tags`, `app_modules`,
`feature_flags`, `metadata_kv`) genuinely are just CRUD and shouldn't be
over-engineered into something fancier. But the domain that actually has
business rules — RBAC — is modeled the *same* way, and that's where it
shows:

- Assigning a role to a user is `POST /user-roles` with a raw
  `{userId, roleId, organizationId}` body. There's no `/users/{id}/roles`
  endpoint that reads like the actual operation.
- Granting a permission to a role is `POST /role-permissions`. No
  `/roles/{id}/permissions:bulk` for granting several at once — the frontend
  (`RolePermissionsMatrix.tsx`) has to make one request per checkbox toggle.
- "Revoke all of a user's access" (their sessions, API keys, and role
  assignments together) doesn't exist as an operation at all — an admin
  would have to hit three different endpoints and remember to do so, with no
  transaction tying them together.
- There's no endpoint that returns "what can user X do right now" as a
  single computed view beyond `/me/permissions`, which only works for the
  *current* caller, not for an admin inspecting someone else's effective
  permissions.

## What I'd suggest instead (not done)

Introduce a use-case layer alongside (not replacing) the generic CRUD
services, specifically for the RBAC domain:

```
business/src/main/java/com/fourati/usecase/rbac/
  AssignRoleToUser.java       -- validates, checks for duplicate/expired grants, one @Audited write
  GrantPermissionsToRole.java -- bulk, one transaction, one audit event, evicts the permissions cache
  RevokeUserAccess.java       -- revokes sessions + api keys + role assignments as one operation
  GetEffectivePermissions.java -- already exists as RolePermissionService.findEffectivePermissionsByRoleNames,
                                   just needs an admin-facing variant that takes a userId
```

Each of these is a small, named, testable class with one public method and
real business rules in it — not a generic `create(request)`. Controllers
call these for the RBAC surface; everything else (tags, settings, menus,
metadata) stays as the existing generic CRUD, which is genuinely fine for
that kind of data.

I didn't build this because it's the kind of decision (naming, package
structure, how much to bite off at once) that's more useful coming from you
than imposed by me in a review pass. Happy to build the first one
(`AssignRoleToUser`) as a concrete example if you want to see the shape
before committing to the rest.

## Smaller architecture notes

- **`AuditAspect.resolveEntityId`** uses reflection
  (`result.getClass().getMethod("id")`) to guess which argument or return
  value is "the entity id" for audit logging. It's clever but fragile — a
  method with two UUID parameters (like
  `RolePermissionService.delete(roleId, permissionId)` conceptually, though
  that one takes a single composite id) would silently audit the wrong
  entity if the signature ever changes. Worth replacing with an explicit
  `@Audited(entityIdParam = "userId")` attribute rather than inferring it.

- **`OpenApiConfig`** still has 4 `GroupedOpenApi` beans
  (`domainGroup`/`documentsGroup` removed by the last pass, but
  `systemGroup`/`allGroup` remain) from what reads like a starter template.
  Worth deciding what actual API groupings make sense for 33 real resources
  — maybe by domain area (RBAC, Org structure, Content, System) — rather
  than the generic Items/Documents split that was there before.

- **No `@Version` / optimistic locking anywhere.** Every entity extends
  `BaseEntity` or `SoftDeletableEntity`, neither of which has a `@Version`
  field. A `PUT` silently overwrites whatever's in the database regardless
  of whether it changed since the client read it. Low effort, meaningful
  correctness improvement: add `version BIGINT NOT NULL DEFAULT 0` to the
  mutable tables via a Flyway migration, add `@Version private long version`
  to `BaseEntity`, and map `ObjectOptimisticLockingFailureException` to 409
  in `GlobalExceptionHandler`.
