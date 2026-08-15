import { useEffect } from 'react';
import AppProviders from '@/app/providers/AppProviders';
import AppRoutes from '@/core/router/AppRoutes';
import OnboardingTour from '@/shared/components/Onboarding/OnboardingTour';
import { useAuthStore } from '@/core/auth/stores/useAuthStore';

export default function App() {
  const init = useAuthStore((s) => s.init);
  useEffect(() => { void init(); }, [init]);

  return (
    <AppProviders>
      <OnboardingTour />
      <AppRoutes />
    </AppProviders>
  );
}
