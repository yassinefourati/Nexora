import type { User as OidcUser } from 'oidc-client-ts';

/** Realm roles the Keycloak protocol mapper exposes as a top-level `roles` claim. */
export function rolesFromOidcUser(user: OidcUser): string[] {
  const claim = (user.profile as Record<string, unknown>).roles;
  return Array.isArray(claim) ? claim.filter((r): r is string => typeof r === 'string') : [];
}
