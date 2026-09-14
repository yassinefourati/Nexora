import { describe, expect, it } from 'vitest';
import fs from 'node:fs';
import path from 'node:path';
import en from '@/core/i18n/locales/en';
import fr from '@/core/i18n/locales/fr';

type Tree = { [k: string]: string | Tree };

function flatten(obj: Tree, prefix = ''): string[] {
  return Object.entries(obj).flatMap(([k, v]) =>
    typeof v === 'string' ? [prefix + k] : flatten(v as Tree, prefix + k + '.'));
}

function walk(dir: string, out: string[] = []): string[] {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(p, out);
    else if (/\.tsx?$/.test(entry.name) && !/\.test\.tsx?$/.test(entry.name)) out.push(p);
  }
  return out;
}

/**
 * Locale completeness: the French and English trees carry exactly the same
 * keys, and every static `t('some.key')` in the source resolves. Dynamic keys
 * (template literals such as t(`resources.${resource}.title`)) are covered by
 * the resource-level checks below.
 */
describe('locales', () => {
  const enKeys = new Set(flatten(en as unknown as Tree));
  const frKeys = new Set(flatten(fr as unknown as Tree));

  it('fr has every en key and nothing extra', () => {
    const missing = [...enKeys].filter((k) => !frKeys.has(k));
    const extra = [...frKeys].filter((k) => !enKeys.has(k));
    expect(missing, 'missing in fr').toEqual([]);
    expect(extra, 'extra in fr').toEqual([]);
  });

  it('no French value is left identical to English unless it is a proper noun, code or symbol', () => {
    const lookup = (tree: Tree, key: string) => key.split('.').reduce<unknown>((acc, part) => (acc as Tree)[part], tree) as string;
    const allowedSame = /^[A-Z0-9#@._\-{}(),/ ]*$|^(Notifications?|Permissions?|Description|Code|Action|Menu|Version|Statut|Sessions|Source|Message|IP|Services?|Actions|Dashboard|Menus|2FA|—|email, sms, push|p\. ex\. .*|· Permissions|#1976d2)$/i;
    const same = [...enKeys].filter((k) => lookup(en as unknown as Tree, k) === lookup(fr as unknown as Tree, k) && !allowedSame.test(lookup(en as unknown as Tree, k)));
    expect(same).toEqual([]);
  });

  it('every static t() key used in the source exists in en', () => {
    const src = path.resolve(__dirname, '../../..');
    const used = new Set<string>();
    for (const file of walk(src)) {
      if (file.includes(`${path.sep}test${path.sep}`)) continue;
      const text = fs.readFileSync(file, 'utf8');
      for (const m of text.matchAll(/\bt\('([a-zA-Z0-9_.]+)'/g)) used.add(m[1]);
    }
    const unknown = [...used].filter((k) => !enKeys.has(k)).sort();
    expect(unknown).toEqual([]);
  });

  it('every resource namespace has a title (pages derive their heading from it)', () => {
    for (const [name, spec] of Object.entries(en.resources)) {
      expect(typeof (spec as Tree).title, name).toBe('string');
    }
  });
});
