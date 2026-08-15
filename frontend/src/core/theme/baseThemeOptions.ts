import type { ThemeOptions } from '@mui/material/styles';

const fontStack = '-apple-system, BlinkMacSystemFont, "Segoe UI", Inter, Roboto, "Helvetica Neue", Arial, sans-serif';

// shared across light/dark/blue — the individual theme files only add palette colors
export const baseThemeOptions: ThemeOptions = {
  shape: { borderRadius: 8 },
  typography: {
    fontFamily: fontStack,
    h1: { fontSize: '2.5rem', fontWeight: 700, lineHeight: 1.2, letterSpacing: '-0.02em' },
    h2: { fontSize: '2rem', fontWeight: 700, lineHeight: 1.25, letterSpacing: '-0.02em' },
    h3: { fontSize: '1.625rem', fontWeight: 700, lineHeight: 1.3, letterSpacing: '-0.01em' },
    h4: { fontSize: '1.375rem', fontWeight: 700, lineHeight: 1.35, letterSpacing: '-0.01em' },
    h5: { fontSize: '1.125rem', fontWeight: 600, lineHeight: 1.4 },
    h6: { fontSize: '1rem', fontWeight: 600, lineHeight: 1.4 },
    subtitle1: { fontSize: '0.9375rem', fontWeight: 600, lineHeight: 1.5 },
    subtitle2: { fontSize: '0.8125rem', fontWeight: 600, lineHeight: 1.5 },
    body1: { fontSize: '0.9375rem', lineHeight: 1.55 },
    body2: { fontSize: '0.8125rem', lineHeight: 1.55 },
    caption: { fontSize: '0.75rem', lineHeight: 1.5 },
    overline: { fontSize: '0.6875rem', fontWeight: 700, letterSpacing: '0.06em' },
    button: { fontWeight: 600, textTransform: 'none' },
  },
  shadows: [
    'none',
    '0 1px 2px rgba(16,24,40,0.06)',
    '0 1px 3px rgba(16,24,40,0.08)',
    '0 2px 4px rgba(16,24,40,0.08)',
    '0 2px 6px rgba(16,24,40,0.09)',
    '0 4px 8px rgba(16,24,40,0.10)',
    '0 4px 10px rgba(16,24,40,0.10)',
    '0 6px 12px rgba(16,24,40,0.11)',
    '0 6px 14px rgba(16,24,40,0.11)',
    '0 8px 16px rgba(16,24,40,0.12)',
    '0 8px 18px rgba(16,24,40,0.12)',
    '0 10px 20px rgba(16,24,40,0.12)',
    '0 10px 22px rgba(16,24,40,0.13)',
    '0 12px 24px rgba(16,24,40,0.13)',
    '0 12px 26px rgba(16,24,40,0.13)',
    '0 14px 28px rgba(16,24,40,0.14)',
    '0 14px 30px rgba(16,24,40,0.14)',
    '0 16px 32px rgba(16,24,40,0.14)',
    '0 16px 34px rgba(16,24,40,0.15)',
    '0 18px 36px rgba(16,24,40,0.15)',
    '0 18px 38px rgba(16,24,40,0.15)',
    '0 20px 40px rgba(16,24,40,0.16)',
    '0 20px 42px rgba(16,24,40,0.16)',
    '0 22px 44px rgba(16,24,40,0.16)',
    '0 22px 46px rgba(16,24,40,0.17)',
  ] as ThemeOptions['shadows'],
  components: {
    MuiCssBaseline: {
      styleOverrides: (theme) => ({
        // Visible, consistent keyboard focus ring app-wide (WCAG 2.2 focus-visible).
        '*:focus-visible': { outline: `2px solid ${theme.palette.primary.main}`, outlineOffset: '2px' },
        '@media (prefers-reduced-motion: reduce)': {
          '*, *::before, *::after': {
            animationDuration: '0.001ms !important',
            animationIterationCount: '1 !important',
            transitionDuration: '0.001ms !important',
          },
        },
      }),
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { borderRadius: 8, fontWeight: 600, paddingInline: 16 },
        sizeSmall: { paddingInline: 12 },
        containedPrimary: { '&:hover': { boxShadow: 'none' } },
      },
    },
    MuiIconButton: {
      styleOverrides: { root: { borderRadius: 8 } },
    },
    MuiPaper: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: { backgroundImage: 'none' },
        outlined: ({ theme }) => ({ borderColor: theme.palette.divider }),
      },
    },
    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: ({ theme }) => ({
          border: `1px solid ${theme.palette.divider}`,
          borderRadius: 12,
        }),
      },
    },
    MuiAppBar: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: ({ theme }) => ({ borderBottom: `1px solid ${theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.08)'}` }),
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: ({ theme }) => ({ borderRight: `1px solid ${theme.palette.divider}`, backgroundImage: 'none' }),
      },
    },
    MuiTextField: { defaultProps: { size: 'small' } },
    MuiOutlinedInput: {
      styleOverrides: {
        root: { borderRadius: 8 },
      },
    },
    MuiSelect: { defaultProps: { size: 'small' } },
    MuiChip: {
      styleOverrides: {
        root: { borderRadius: 6, fontWeight: 600 },
        sizeSmall: { fontSize: '0.6875rem' },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: ({ theme }) => ({ borderBottom: `1px solid ${theme.palette.divider}`, padding: '10px 16px' }),
        head: ({ theme }) => ({ fontWeight: 700, fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.04em', color: theme.palette.text.secondary }),
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: { '&:last-of-type td': { borderBottom: 'none' } },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: { fontSize: '0.75rem', borderRadius: 6, fontWeight: 500 },
      },
    },
    MuiDialog: {
      styleOverrides: { paper: { borderRadius: 16 } },
    },
    MuiMenu: {
      styleOverrides: {
        paper: ({ theme }) => ({ borderRadius: 10, border: `1px solid ${theme.palette.divider}` }),
      },
    },
    MuiMenuItem: {
      styleOverrides: { root: { borderRadius: 6, marginInline: 4, marginBlock: 1 } },
    },
    MuiAlert: {
      styleOverrides: { root: { borderRadius: 8 } },
    },
    MuiListItemButton: {
      styleOverrides: { root: { borderRadius: 8 } },
    },
  },
};
