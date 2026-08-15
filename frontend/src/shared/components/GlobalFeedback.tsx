import { Snackbar, Alert, Box, Button, Fade } from '@mui/material';
import { useAppStore } from '@/shared/stores/useAppStore';

export default function GlobalFeedback() {
  const { loading, snackbar, closeSnackbar } = useAppStore();
  const showUndo = snackbar.message.includes('Click Undo');

  const handleUndo = () => {
    const fn = (window as unknown as Record<string, unknown>).__undoDelete;
    if (typeof fn === 'function') (fn as () => void)();
    closeSnackbar();
  };

  return (
    <>
      {/* Top loading bar — thin, fast, smooth */}
      <Fade in={loading}>
        <Box
          sx={{
            position: 'fixed', top: 0, left: 0, right: 0, zIndex: 2000,
            height: 3,
            background: (t) => `linear-gradient(90deg, ${t.palette.primary.main}, ${t.palette.primary.light})`,
            backgroundSize: '200% 100%',
            animation: 'loadingSlide 1.2s ease infinite',
            '@keyframes loadingSlide': {
              '0%':   { backgroundPosition: '200% 0' },
              '100%': { backgroundPosition: '-200% 0' },
            },
            boxShadow: (t) => `0 0 10px ${t.palette.primary.main}55`,
          }}
        />
      </Fade>

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={showUndo ? 5500 : 4000}
        onClose={closeSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        TransitionProps={{ timeout: 250 }}
      >
        <Alert
          severity={snackbar.severity}
          onClose={closeSnackbar}
          variant="filled"
          sx={{ minWidth: 280, boxShadow: 4 }}
          action={
            showUndo
              ? <Button color="inherit" size="small" onClick={handleUndo} sx={{ fontWeight: 700 }}>UNDO</Button>
              : undefined
          }
        >
          {snackbar.message.replace(' Click Undo to reverse.', '')}
        </Alert>
      </Snackbar>
    </>
  );
}
