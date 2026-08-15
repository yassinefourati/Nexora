import { lightTheme } from './lightTheme';
import { blueTheme } from './blueTheme';
import { darkTheme } from './darkTheme';
export type ThemeMode = 'light' | 'blue' | 'dark';
export const getTheme = (mode: ThemeMode) => mode === 'dark' ? darkTheme : mode === 'blue' ? blueTheme : lightTheme;
