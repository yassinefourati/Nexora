import DOMPurify from 'dompurify';

/**
 * Strip HTML tags and dangerous content from any user-supplied string.
 * Use on every field that gets rendered or stored.
 */
export function sanitize(value: string): string {
  // DOMPurify removes tags; we also strip control chars (non-printable ASCII)
  const clean = DOMPurify.sanitize(value, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] });
  // eslint-disable-next-line no-control-regex
  return clean.replace(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g, '').trim();
}

/** Sanitize every string value in a plain object (one level deep) */
export function sanitizeObject<T extends Record<string, unknown>>(obj: T): T {
  return Object.fromEntries(
    Object.entries(obj).map(([k, v]) => [k, typeof v === 'string' ? sanitize(v) : v])
  ) as T;
}
