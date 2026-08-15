import type { Page } from '@playwright/test';
import { expect } from '@playwright/test';

/** Dismisses the first-run onboarding tour dialog if it's showing — it can intercept clicks underneath it. */
export async function dismissOnboardingTour(page: Page) {
  const skipTour = page.getByRole('button', { name: /skip tour/i });
  try {
    await skipTour.waitFor({ state: 'visible', timeout: 3_000 });
    await skipTour.click();
    await skipTour.waitFor({ state: 'hidden', timeout: 3_000 });
  } catch {
    // Tour didn't appear (e.g. already dismissed in a prior run) — nothing to do.
  }
}

/**
 * Drives the real OIDC Authorization Code + PKCE flow: clicking "Login"
 * redirects the browser to Keycloak's hosted login page, which we fill in
 * directly, then Keycloak redirects back to /auth/callback and finally the
 * app's home route. There is no local password form to fill anymore —
 * Keycloak owns the entire login UI.
 */
export async function loginAsSuperadmin(page: Page) {
  await page.goto('/login');
  await dismissOnboardingTour(page);
  await page.getByRole('button', { name: /login/i }).click();
  await page.waitForURL(/realms\/fourati-realm\/protocol\/openid-connect\/auth/);
  await page.fill('#username', 'superadmin');
  await page.fill('#password', 'ChangeMe123!');
  await page.click('#kc-login');
  await page.waitForURL('http://localhost:5173/', { timeout: 15_000 });
  await expect(page).toHaveURL('/');
}
