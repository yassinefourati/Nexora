import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import i18next from 'eslint-plugin-i18next';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  { ignores: ['dist', 'coverage', 'playwright-report', 'test-results'] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ['**/*.{ts,tsx}'],
    languageOptions: { ecmaVersion: 2020, globals: globals.browser },
    plugins: { 'react-hooks': reactHooks, 'react-refresh': reactRefresh },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
      '@typescript-eslint/no-explicit-any': 'error',
      'react-hooks/exhaustive-deps': 'warn',
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
      '@typescript-eslint/consistent-type-imports': ['error', { prefer: 'type-imports' }],
    },
  },
  {
    // Architecture boundary: UI never talks HTTP. The client is used by the data
    // layer only (core/api, core/auth, features/*/api, features/*/hooks); pages,
    // components and layout consume hooks.
    files: ['src/features/**/pages/**', 'src/features/**/components/**', 'src/layout/**', 'src/shared/components/**', 'src/app/**'],
    rules: {
      'no-restricted-imports': ['error', {
        paths: [{ name: '@/core/api/client', message: 'UI code must not call the API client; use a hook from features/<x>/hooks.' }],
      }],
    },
  },
  {
    // Every user-facing string in feature and layout code goes through i18n.
    files: ['src/features/**/*.tsx', 'src/layout/**/*.tsx', 'src/shared/components/**/*.tsx'],
    ignores: ['src/features/devdocs/**', 'src/features/uidocs/**', 'src/features/guide/**'],
    plugins: { i18next },
    rules: {
      'i18next/no-literal-string': ['error', {
        mode: 'jsx-only',
        'jsx-attributes': { include: ['label', 'title', 'placeholder', 'helperText', 'aria-label', 'description', 'confirmLabel', 'headerName', 'message', 'primary', 'secondary'] },
        'should-validate-template': true,
        // form field names, routes and store keys are identifiers, not copy
        callees: { exclude: ['register', 'watch', 'useWatch', 'setValue', 'getValues', 'trigger', 'resetField', 'setError', 'clearErrors', 'navigate', 't', 'i18n.*', 'console.*', 'localStorage.*', 'sessionStorage.*', 'queryKey', 'invalidateQueries', 'setQueryData', 'addRecentPage', 'useUsers', 'useDepartments', 'useOrganizations', 'useTags', 'useRoles', 'useMenuItems', 'usePermissionsList', 'useMenus', 'useTeams'] },
      }],
    },
  },
);
