import { test, expect } from '@playwright/test';
import { loginAsSuperadmin } from './helpers/login';

test.beforeEach(async ({ page }) => {
  await loginAsSuperadmin(page);
  await page.goto('/users');
});

test.describe('Users CRUD', () => {
  test('users table loads with real backend data', async ({ page }) => {
    await expect(page.getByRole('cell', { name: 'superadmin', exact: true })).toBeVisible();
  });

  test('opens add user dialog', async ({ page }) => {
    await page.getByRole('button', { name: /add user/i }).click();
    const dialog = page.getByRole('dialog');
    await expect(dialog).toBeVisible();
    await expect(dialog.getByRole('heading', { name: 'Add User' })).toBeVisible();
  });

  test('creates a new user against the real backend', async ({ page }) => {
    const username = `e2e-user-${Date.now()}`;
    await page.getByRole('button', { name: /add user/i }).click();
    const dialog = page.getByRole('dialog');
    await dialog.getByLabel('Username').fill(username);
    await dialog.getByLabel('First name').fill('E2E');
    await dialog.getByLabel('Last name').fill('Test');
    await dialog.getByLabel('Email').fill(`${username}@example.com`);
    await dialog.getByLabel('Password').fill('password123');
    await dialog.getByRole('button', { name: 'Create' }).click();
    await expect(page.getByText('Created successfully')).toBeVisible();
    await expect(page.getByRole('cell', { name: username, exact: true })).toBeVisible();
  });

  test('opens confirm dialog before deleting a user', async ({ page }) => {
    const row = page.getByRole('row').filter({ hasText: 'superadmin' });
    await row.getByRole('button', { name: /more actions/i }).click();
    await page.getByRole('menuitem', { name: /delete/i }).click();
    await expect(page.getByRole('dialog')).toBeVisible();
  });
});
