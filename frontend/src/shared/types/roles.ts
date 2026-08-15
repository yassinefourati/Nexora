/**
 * Real backend permission vocabulary — confirmed against live /api/v1/permissions:
 * resources are singular (user, role, menu, settings, audit, report), actions are
 * read/write/edit/delete/approve. This replaces an earlier fictional vocabulary
 * (users/users.roles/settings.database + view/create/edit/delete) that never
 * matched any real backend permission.
 */
export type Resource = 'user' | 'role' | 'menu' | 'settings' | 'audit' | 'report';
export type Action = 'read' | 'write' | 'edit' | 'delete' | 'approve';

/** Keycloak realm roles (see backend/keycloak/fourati-realm.json). */
export type KeycloakRole = 'admin' | 'manager' | 'user';
