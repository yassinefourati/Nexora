import { test, expect } from '@playwright/test';
import { loginAsSuperadmin } from './helpers/login';

test.beforeEach(async ({ page }) => {
  await loginAsSuperadmin(page);
});

test.describe('Navigation', () => {
  test('navigates to Users page', async ({ page }) => {
    await page.goto('/users');
    await expect(page.getByRole('heading', { name: 'Users', level: 1 })).toBeVisible();
  });

  test('navigates to Roles page', async ({ page }) => {
    await page.goto('/users/roles');
    await expect(page.getByText('Manage roles that can be assigned to users.')).toBeVisible();
  });

  test('navigates to Organizations page', async ({ page }) => {
    await page.goto('/organizations');
    await expect(page.getByRole('heading', { name: 'Organizations', level: 1 })).toBeVisible();
  });

  test('navigates to Settings page', async ({ page }) => {
    await page.goto('/settings');
    await expect(page.getByText('Global and per-organization configuration key/value pairs.')).toBeVisible();
  });

  test('navigates to Profile page', async ({ page }) => {
    await page.goto('/profile');
    await expect(page.getByText('Account details')).toBeVisible();
  });

  test('navigates to Audit Log page', async ({ page }) => {
    await page.goto('/audit');
    await expect(page.getByRole('heading', { name: 'Audit Log', level: 1 })).toBeVisible();
  });

  test('404 page shown for unknown route', async ({ page }) => {
    await page.goto('/this-does-not-exist');
    await expect(page.getByText('404')).toBeVisible();
  });
});
