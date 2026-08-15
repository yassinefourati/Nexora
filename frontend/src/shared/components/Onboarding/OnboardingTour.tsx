import { useState } from 'react';
import { Dialog, DialogContent, DialogActions, Button, Typography, Box, MobileStepper } from '@mui/material';
import KeyboardArrowLeftIcon from '@mui/icons-material/KeyboardArrowLeft';
import KeyboardArrowRightIcon from '@mui/icons-material/KeyboardArrowRight';
import DashboardIcon from '@mui/icons-material/Dashboard';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import SearchIcon from '@mui/icons-material/Search';
import { useUIStore } from '@/shared/stores/useUIStore';

const STEPS = [
  {
    icon: <DashboardIcon sx={{ fontSize: 48, color: 'primary.main' }} />,
    title: 'Welcome to Admin Panel 👋',
    body: 'Your dashboard shows live stats, a recent activity feed, and weekly signups. Stats refresh automatically every 15–30 seconds.',
  },
  {
    icon: <SearchIcon sx={{ fontSize: 48, color: 'primary.main' }} />,
    title: 'Navigate instantly with ⌘K',
    body: 'Press Cmd+K (or Ctrl+K) to open the global search. Type any page name to jump there instantly. Press ? to see all keyboard shortcuts.',
  },
  {
    icon: <MenuBookIcon sx={{ fontSize: 48, color: 'primary.main' }} />,
    title: 'Adding new features is easy',
    body: 'Open the Dev Guide in the sidebar (bottom section). It has a live demo of the AdvancedDataTable and step-by-step code for adding any new CRUD feature in 5 files.',
  },
];

export default function OnboardingTour() {
  const { seenOnboarding, setSeenOnboarding } = useUIStore();
  const [step, setStep] = useState(0);

  if (seenOnboarding) return null;

  const isLast = step === STEPS.length - 1;
  const current = STEPS[step];

  return (
    <Dialog open maxWidth="xs" fullWidth aria-labelledby="onboarding-tour-title" PaperProps={{ sx: { borderRadius: 3 } }}>
      <DialogContent sx={{ textAlign: 'center', pt: 4, pb: 2 }}>
        <Box sx={{ mb: 2 }}>{current.icon}</Box>
        <Typography id="onboarding-tour-title" variant="h6" fontWeight={700} mb={1.5}>{current.title}</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.7 }}>{current.body}</Typography>
      </DialogContent>

      <MobileStepper
        variant="dots"
        steps={STEPS.length}
        position="static"
        activeStep={step}
        sx={{ justifyContent: 'center', bgcolor: 'transparent', pb: 1 }}
        nextButton={null}
        backButton={null}
      />

      <DialogActions sx={{ px: 3, pb: 3, justifyContent: 'space-between' }}>
        <Button size="small" color="inherit" onClick={setSeenOnboarding} sx={{ color: 'text.secondary' }}>
          Skip tour
        </Button>
        <Box sx={{ display: 'flex', gap: 1 }}>
          {step > 0 && (
            <Button size="small" startIcon={<KeyboardArrowLeftIcon />} onClick={() => setStep((s) => s - 1)}>
              Back
            </Button>
          )}
          <Button
            variant="contained"
            size="small"
            endIcon={isLast ? null : <KeyboardArrowRightIcon />}
            onClick={() => isLast ? setSeenOnboarding() : setStep((s) => s + 1)}
          >
            {isLast ? "Let's go!" : 'Next'}
          </Button>
        </Box>
      </DialogActions>
    </Dialog>
  );
}
