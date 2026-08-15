import { Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button, LinearProgress, Box } from '@mui/material';
import { useEffect, useState } from 'react';

interface Props { open: boolean; warningSeconds: number; onStayLoggedIn: () => void; onLogout: () => void; }

function CountdownContent({ warningSeconds, onStayLoggedIn, onLogout }: Omit<Props, 'open'>) {
  const [remaining, setRemaining] = useState(warningSeconds);
  useEffect(() => {
    const interval = setInterval(() => {
      setRemaining((r) => { if (r <= 1) { clearInterval(interval); return 0; } return r - 1; });
    }, 1000);
    return () => clearInterval(interval);
  }, []);
  return (
    <>
      <DialogTitle>Session expiring soon</DialogTitle>
      <DialogContent>
        <DialogContentText>You'll be logged out in <strong>{remaining}s</strong> due to inactivity.</DialogContentText>
        <Box sx={{ mt: 2 }}>
          <LinearProgress variant="determinate" value={(remaining / warningSeconds) * 100} color={remaining < 10 ? 'error' : 'primary'} />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button color="error" onClick={onLogout}>Logout now</Button>
        <Button variant="contained" onClick={onStayLoggedIn} autoFocus>Stay logged in</Button>
      </DialogActions>
    </>
  );
}

export default function SessionTimeoutDialog({ open, warningSeconds, onStayLoggedIn, onLogout }: Props) {
  return (
    <Dialog open={open} maxWidth="xs" fullWidth>
      {/* Remount on open to reset countdown cleanly — no setState in effect */}
      {open && <CountdownContent warningSeconds={warningSeconds} onStayLoggedIn={onStayLoggedIn} onLogout={onLogout} />}
    </Dialog>
  );
}
