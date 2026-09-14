-- Authentication is delegated to Keycloak (OIDC). The backend never verifies a
-- local password, so requiring one on every users row only forced callers to
-- invent a throwaway secret that was hashed and never used. Keep the column for
-- accounts that were created with one, but stop requiring it.
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;
