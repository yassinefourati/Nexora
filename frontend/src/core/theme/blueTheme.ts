import { createTheme } from '@mui/material/styles';
import { baseThemeOptions } from './baseThemeOptions';

export const blueTheme = createTheme({
  ...baseThemeOptions,
  palette: {
    mode: 'light',
    primary: { main: '#0d47a1', dark: '#092e6b', light: '#4b6fbe' },
    secondary: { main: '#7c3aed' },
    success: { main: '#15784a' },
    warning: { main: '#b76e00' },
    error: { main: '#d32f2f' },
    info: { main: '#0288d1' },
    background: { default: '#eef3fb', paper: '#ffffff' },
    divider: 'rgba(13,71,161,0.14)',
    text: { primary: '#0f1b33', secondary: '#4b5b76' },
  },
});
