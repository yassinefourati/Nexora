/**
 * Feature flag hook backed by VITE_FLAGS env var (JSON string).
 *
 * Usage in .env:
 *   VITE_FLAGS={"bulk-delete":true,"sso-login":false,"reports-page":true}
 *
 * Usage in code:
 *   const canBulkDelete = useFeatureFlag('bulk-delete');
 */

let parsed: Record<string, boolean> | null = null;

function getFlags(): Record<string, boolean> {
  if (parsed) return parsed;
  try {
    const raw = (import.meta.env as Record<string, string>).VITE_FLAGS;
    parsed = raw ? (JSON.parse(raw) as Record<string, boolean>) : {};
  } catch { parsed = {}; }
  return parsed;
}

export function useFeatureFlag(flag: string, defaultValue = false): boolean {
  const flags = getFlags();
  return flag in flags ? flags[flag] : defaultValue;
}

export function isEnabled(flag: string, defaultValue = false): boolean {
  return getFlags()[flag] ?? defaultValue;
}
