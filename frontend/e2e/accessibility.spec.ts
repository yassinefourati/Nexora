import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import type { Page } from '@playwright/test';
import { loginAsSuperadmin } from './helpers/login';

async function expectNoA11yViolations(page: Page) {
  const results = await new AxeBuilder({ page })
    .exclude('#webpack-dev-server-client-overlay')
    .analyze();
  expect(results.violations, results.violations.map((v) => `${v.id}: ${v.description}`).join('\n')).toHaveLength(0);
}

test.describe('Accessibility', () => {
  test('login page has no violations', async ({ page }) => {
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
    await expectNoA11yViolations(page);
  });

  test('dashboard has no violations', async ({ page }) => {
    await loginAsSuperadmin(page);
    await page.waitForLoadState('networkidle');
    await expectNoA11yViolations(page);
  });

  test('users page has no violations', async ({ page }) => {
    await loginAsSuperadmin(page);
    await page.goto('/users');
    await page.waitForLoadState('networkidle');
    await expectNoA11yViolations(page);
  });

  test('organizations page has no violations', async ({ page }) => {
    await loginAsSuperadmin(page);
    await page.goto('/organizations');
    await page.waitForLoadState('networkidle');
    await expectNoA11yViolations(page);
  });

  test('settings page has no violations', async ({ page }) => {
    await loginAsSuperadmin(page);
    await page.goto('/settings');
    await page.waitForLoadState('networkidle');
    await expectNoA11yViolations(page);
  });

  test('notifications page has no violations', async ({ page }) => {
    await loginAsSuperadmin(page);
    await page.goto('/notifications');
    await page.waitForLoadState('networkidle');
    await expectNoA11yViolations(page);
  });

  test('404 page has no violations', async ({ page }) => {
    await page.goto('/this-does-not-exist');
    await page.waitForLoadState('networkidle');
    await expectNoA11yViolations(page);
  });
});
