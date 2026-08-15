import { AppBar, Toolbar, IconButton, Typography, Avatar, Menu, MenuItem, Tooltip } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import LightModeIcon from '@mui/icons-material/LightMode';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import PaletteIcon from '@mui/icons-material/Palette';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useUIStore } from '@/shared/stores/useUIStore';
import { useAuthStore } from '@/core/auth/stores/useAuthStore';
import { ROUTES } from '@/core/router/routes';
import NotificationsBell from '@/features/notifications/components/NotificationsBell';
import KeyboardShortcutsModal from '@/shared/components/KeyboardShortcutsModal';
import { useKeyboardShortcuts } from '@/shared/hooks/useKeyboardShortcuts';
const THEME_ICONS = { light: <LightModeIcon />, blue: <PaletteIcon />, dark: <DarkModeIcon /> };
export default function Navbar() {
  const { toggleSidebar, toggleTheme, theme, language, setLanguage } = useUIStore();
  const { logout, user } = useAuthStore();
  const navigate = useNavigate();
  const { i18n } = useTranslation();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [helpOpen, setHelpOpen] = useState(false);
  const handleLogout = () => { setAnchorEl(null); void logout(); };
  const handleToggleLang = () => { const next = language === 'en' ? 'fr' : 'en'; setLanguage(next); i18n.changeLanguage(next); };
  useKeyboardShortcuts(() => {}, () => setHelpOpen(true));
  return (
    <>
      <AppBar position="fixed" sx={{ zIndex: (t) => t.zIndex.drawer + 1 }}>
        <Toolbar>
          <IconButton color="inherit" onClick={toggleSidebar} edge="start" sx={{ mr: 1 }} aria-label="Toggle sidebar"><MenuIcon /></IconButton>
          <Typography component="span" variant="h6" noWrap sx={{ flexGrow: 1 }}>Admin Panel</Typography>
          <NotificationsBell />
          <Tooltip title={language === 'en' ? 'Switch to French' : 'Passer en anglais'}>
            <IconButton color="inherit" onClick={handleToggleLang} aria-label={language === 'en' ? 'Switch to French' : 'Passer en anglais'} sx={{ fontSize:'0.75rem', fontWeight:700, width:36, height:36 }}>{language === 'en' ? 'EN' : 'FR'}</IconButton>
          </Tooltip>
          <Tooltip title={`Theme: ${theme}`}><IconButton color="inherit" onClick={toggleTheme} aria-label={`Theme: ${theme}`}>{THEME_ICONS[theme]}</IconButton></Tooltip>
          <Tooltip title="Keyboard shortcuts (?)"><IconButton color="inherit" onClick={() => setHelpOpen(true)} aria-label="Keyboard shortcuts">{<HelpOutlineIcon />}</IconButton></Tooltip>
          <IconButton
            onClick={(e) => setAnchorEl(e.currentTarget)}
            aria-label="Account menu"
            aria-haspopup="true"
            aria-expanded={Boolean(anchorEl)}
            sx={{ p: 0, ml: 1 }}
          >
            <Avatar sx={{ width:32, height:32, bgcolor:'primary.dark' }}>{user?.name?.[0]?.toUpperCase()}</Avatar>
          </IconButton>
          <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)} transformOrigin={{ horizontal:'right', vertical:'top' }} anchorOrigin={{ horizontal:'right', vertical:'bottom' }}>
            <MenuItem disabled sx={{ fontSize:'0.8rem', opacity:'1 !important' }}>{user?.name} · {user?.roles.join(', ')}</MenuItem>
            <MenuItem onClick={() => { setAnchorEl(null); navigate(ROUTES.PROFILE); }}>Profile</MenuItem>
            <MenuItem onClick={handleLogout}>Logout</MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      <KeyboardShortcutsModal open={helpOpen} onClose={() => setHelpOpen(false)} />
    </>
  );
}
