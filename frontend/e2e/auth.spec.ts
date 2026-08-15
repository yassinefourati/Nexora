import { test, expect } from '@playwright/test';
import { loginAsSuperadmin, dismissOnboardingTour } from './helpers/login';

test.describe('Authentication', () => {
  test('login redirects to Keycloak and back to the dashboard', async ({ page }) => {
    await loginAsSuperadmin(page);
    await expect(page.getByRole('heading', { name: /dashboard/i })).toBeVisible();
  });

  test('invalid Keycloak credentials show an error on the identity provider page', async ({ page }) => {
    await page.goto('/login');
    await dismissOnboardingTour(page);
    await page.getByRole('button', { name: /login/i }).click();
    await page.waitForURL(/realms\/fourati-realm\/protocol\/openid-connect\/auth/);
    await page.fill('#username', 'superadmin');
    await page.fill('#password', 'wrong-password');
    await page.click('#kc-login');
    await expect(page.locator('#input-error, .kc-feedback-text, .alert-error')).toBeVisible();
  });

  test('protected route redirects to login when unauthenticated', async ({ page }) => {
    await page.goto('/users');
    await expect(page).toHaveURL(/\/login$/);
  });

  test('logout clears the session and returns to login', async ({ page }) => {
    await loginAsSuperadmin(page);
    await page.getByRole('button', { name: 'Account menu' }).click();
    await page.getByRole('menuitem', { name: /logout/i }).click();
    await page.waitForURL(/\/login$/);
    await page.waitForLoadState('networkidle');
    await page.goto('/users');
    await expect(page).toHaveURL(/\/login$/);
  });
});
