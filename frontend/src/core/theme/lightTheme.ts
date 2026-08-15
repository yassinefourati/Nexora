import { createTheme } from '@mui/material/styles';
import { baseThemeOptions } from './baseThemeOptions';

export const lightTheme = createTheme({
  ...baseThemeOptions,
  palette: {
    mode: 'light',
    primary: { main: '#1976d2', dark: '#115293', light: '#4791db' },
    secondary: { main: '#7c3aed' },
    success: { main: '#15784a' },
    warning: { main: '#b76e00' },
    error: { main: '#d32f2f' },
    info: { main: '#0288d1' },
    background: { default: '#f7f8fa', paper: '#ffffff' },
    divider: 'rgba(15,23,42,0.09)',
    text: { primary: '#111827', secondary: '#5b6472' },
  },
});
