import { Dialog, DialogContent, TextField, List, ListItemButton, ListItemText, ListItemIcon, Typography, Box, Chip, InputAdornment, Divider } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { useState, useRef, useEffect } from 'react';
import { useGlobalSearch } from '../hooks/useGlobalSearch';
import type { SearchItem } from '../hooks/useGlobalSearch';

interface Props { open: boolean; onClose: () => void; }

function SearchModalInner({ onClose }: { onClose: () => void }) {
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState(0);
  const { results, navigate_to } = useGlobalSearch(query);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => { inputRef.current?.focus(); }, []);

  const handleSelect = (item: SearchItem) => { navigate_to(item); onClose(); };
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') { e.preventDefault(); setSelected((s) => Math.min(s + 1, results.length - 1)); }
    if (e.key === 'ArrowUp')   { e.preventDefault(); setSelected((s) => Math.max(s - 1, 0)); }
    if (e.key === 'Enter' && results[selected]) handleSelect(results[selected]);
    if (e.key === 'Escape') onClose();
  };

  return (
    <DialogContent sx={{ p: 0 }}>
      <TextField inputRef={inputRef} fullWidth placeholder="Search pages, users, settings… (↑↓ to navigate)"
        value={query} onChange={(e) => { setQuery(e.target.value); setSelected(0); }} onKeyDown={handleKeyDown}
        InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment>, disableUnderline: true }}
        variant="standard" sx={{ '& .MuiInputBase-root': { px: 2, py: 1.5 } }} />
      <Divider />
      {results.length === 0
        ? <Box sx={{ p: 4, textAlign: 'center', color: 'text.secondary' }}><Typography variant="body2">No results for "{query}"</Typography></Box>
        : <List disablePadding sx={{ maxHeight: 400, overflow: 'auto' }}>
            {results.map((item, i) => (
              <ListItemButton key={item.id} selected={i === selected} onClick={() => handleSelect(item)} onMouseEnter={() => setSelected(i)} sx={{ px: 2, py: 1 }}>
                <ListItemIcon sx={{ minWidth: 36, fontSize: '1.2rem' }}>{item.icon}</ListItemIcon>
                <ListItemText primary={item.label} secondary={item.description} primaryTypographyProps={{ fontSize: '0.9rem' }} />
                <Chip label={item.category} size="small" variant="outlined" color={item.category==='user'?'primary':item.category==='recent'?'default':'default'} sx={{ fontSize: '0.65rem', height: 20 }} />
              </ListItemButton>
            ))}
          </List>}
      <Divider />
      <Box sx={{ px: 2, py: 1, display: 'flex', gap: 2, color: 'text.secondary' }}>
        <Typography variant="caption">↑↓ navigate</Typography>
        <Typography variant="caption">↵ select</Typography>
        <Typography variant="caption">esc close</Typography>
        <Typography variant="caption" sx={{ ml: 'auto' }}>⌘K to open</Typography>
      </Box>
    </DialogContent>
  );
}

export default function GlobalSearchModal({ open, onClose }: Props) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth PaperProps={{ sx: { mt: '10vh', verticalAlign: 'top', borderRadius: 3 } }}>
      {/* key forces remount on open, resetting query/selection cleanly */}
      {open && <SearchModalInner onClose={onClose} />}
    </Dialog>
  );
}
