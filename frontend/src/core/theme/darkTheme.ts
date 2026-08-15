import { createTheme } from '@mui/material/styles';
import { baseThemeOptions } from './baseThemeOptions';

export const darkTheme = createTheme({
  ...baseThemeOptions,
  palette: {
    mode: 'dark',
    primary: { main: '#7db8f0', dark: '#4f96e0', light: '#a6d0f5' },
    secondary: { main: '#b794f6' },
    success: { main: '#4ade80' },
    warning: { main: '#facc15' },
    error: { main: '#f87171' },
    info: { main: '#60a5fa' },
    background: { default: '#0b0d10', paper: '#15181d' },
    divider: 'rgba(255,255,255,0.09)',
    text: { primary: '#e8eaed', secondary: '#9aa2ad' },
  },
});
