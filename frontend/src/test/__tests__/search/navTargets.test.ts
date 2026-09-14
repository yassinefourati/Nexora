import { describe, expect, it } from 'vitest';
import { NAV_TARGETS } from '@/features/search/hooks/useGlobalSearch';
import { ROUTES, GUIDE_ROUTE } from '@/core/router/routes';
import en from '@/core/i18n/locales/en';

/**
 * The command palette must never offer a destination that is not routed
 * (it once listed /reports and /invite, both 404s), and every label must be a
 * real translation key.
 */
describe('global search navigation targets', () => {
  const registered = new Set<string>([...Object.values(ROUTES), GUIDE_ROUTE]);

  it('only points at registered routes', () => {
    for (const target of NAV_TARGETS) {
      expect(registered.has(target.path), `${target.id} -> ${target.path}`).toBe(true);
    }
  });

  it('uses labels that exist in the locale', () => {
    const lookup = (key: string) => key.split('.').reduce<unknown>((acc, part) => (acc as Record<string, unknown> | undefined)?.[part], en);
    for (const target of NAV_TARGETS) {
      expect(typeof lookup(target.labelKey), `${target.id} label ${target.labelKey}`).toBe('string');
    }
  });

  it('has unique ids and paths', () => {
    expect(new Set(NAV_TARGETS.map((t) => t.id)).size).toBe(NAV_TARGETS.length);
    expect(new Set(NAV_TARGETS.map((t) => t.path)).size).toBe(NAV_TARGETS.length);
  });
});
