import { Dialog, DialogTitle, DialogContent, Box, Typography, Grid, Chip, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

interface Props { open: boolean; onClose: () => void; }

const SHORTCUTS = [
  { keys: ['⌘', 'K'], description: 'Open global search' },
  { keys: ['?'], description: 'Show keyboard shortcuts' },
  { keys: ['G', 'D'], description: 'Go to Dashboard' },
  { keys: ['G', 'U'], description: 'Go to Users' },
  { keys: ['G', 'S'], description: 'Go to Settings' },
  { keys: ['G', 'P'], description: 'Go to Profile' },
  { keys: ['G', 'N'], description: 'Go to Notifications' },
  { keys: ['G', 'L'], description: 'Go to Audit Log' },
  { keys: ['G', 'I'], description: 'Go to Dev Guide' },
];

export default function KeyboardShortcutsModal({ open, onClose }: Props) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        Keyboard Shortcuts
        <IconButton size="small" onClick={onClose}><CloseIcon /></IconButton>
      </DialogTitle>
      <DialogContent>
        <Grid container spacing={1.5}>
          {SHORTCUTS.map((s, i) => (
            <Grid key={i} size={{ xs: 12, sm: 6 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', p: 1, borderRadius: 1, bgcolor: 'action.hover' }}>
                <Typography variant="body2" color="text.secondary">{s.description}</Typography>
                <Box sx={{ display: 'flex', gap: 0.5 }}>
                  {s.keys.map((k) => <Chip key={k} label={k} size="small" variant="outlined" sx={{ fontFamily: 'monospace', height: 22, fontSize: '0.7rem' }} />)}
                </Box>
              </Box>
            </Grid>
          ))}
        </Grid>
      </DialogContent>
    </Dialog>
  );
}
