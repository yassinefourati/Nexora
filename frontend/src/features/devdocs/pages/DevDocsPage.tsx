import { Box, Grid, Typography, Alert, Chip, Stack, Paper, Button } from '@mui/material';
import { useState, useEffect } from 'react';
import DocSection from '../components/DocSection';
import DocCodeBlock from '../components/DocCodeBlock';
import DocPropTable from '../components/DocPropTable';
import DocLivePreview from '../components/DocLivePreview';
import DocNavSidebar, { type NavItem } from '../components/DocNavSidebar';

import StatCard from '@/features/dashboard/components/StatCard';
import Can from '@/shared/components/Can';
import EmptyState from '@/shared/components/EmptyState/EmptyState';
import AdvancedDataTable from '@/shared/components/AdvancedDataTable/AdvancedDataTable';
import PeopleIcon from '@mui/icons-material/People';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';

const NAV_ITEMS: NavItem[] = [
  { id: 'getting-started', label: 'Getting Started' },
  { id: 'architecture',    label: 'Architecture' },
  {
    id: 'components', label: 'Components', children: [
      { id: 'comp-statcard',  label: 'StatCard' },
      { id: 'comp-table',     label: 'AdvancedDataTable' },
      { id: 'comp-can',       label: 'Can (RBAC)' },
      { id: 'comp-emptystate',label: 'EmptyState' },
      { id: 'comp-confirm',   label: 'ConfirmDialog' },
    ]
  },
  {
    id: 'hooks', label: 'Hooks', children: [
      { id: 'hook-permission',  label: 'usePermission' },
      { id: 'hook-featureflag', label: 'useFeatureFlag' },
      { id: 'hook-debounce',    label: 'useDebounce' },
      { id: 'hook-pagetitle',   label: 'usePageTitle' },
      { id: 'hook-sse',         label: 'useSSE' },
      { id: 'hook-offline',     label: 'useOfflineQueue' },
    ]
  },
  {
    id: 'patterns', label: 'Patterns', children: [
      { id: 'pattern-crud',       label: 'CRUD Feature' },
      { id: 'pattern-optimistic', label: 'Optimistic Updates' },
      { id: 'pattern-stale',      label: 'Cache Strategy' },
      { id: 'pattern-auth',       label: 'Protected Routes' },
    ]
  },
  { id: 'stores',    label: 'Stores' },
  { id: 'api-layer', label: 'API Layer' },
  { id: 'i18n',      label: 'i18n' },
  { id: 'testing',   label: 'Testing' },
  { id: 'env',       label: 'Environment' },
];

interface DemoRow { id: number; name: string; role: string; status: 'active' | 'inactive'; score: number; }
const DEMO_ROWS: DemoRow[] = Array.from({ length: 22 }, (_, i) => ({
  id: i + 1,
  name: ['Alice Martin','Bob Tremblay','Claire Dubois','David Lavoie','Eva Bergeron'][i % 5],
  role: ['admin','editor','viewer'][i % 3],
  status: i % 5 === 0 ? 'inactive' : 'active',
  score: Math.floor(Math.random() * 100),
}));
const DEMO_COLS: ColumnDef<DemoRow>[] = [
  { field: 'id',     headerName: 'ID',     width: 60 },
  { field: 'name',   headerName: 'Name',   flex: 1 },
  { field: 'role',   headerName: 'Role',   width: 100, renderCell: (r) => <Chip label={r.role} size="small" color={r.role === 'admin' ? 'error' : r.role === 'editor' ? 'warning' : 'default'} variant="outlined" /> },
  { field: 'status', headerName: 'Status', width: 110, renderCell: (r) => <Chip label={r.status} size="small" color={r.status === 'active' ? 'success' : 'default'} />, exportValue: (r) => r.status },
  { field: 'score',  headerName: 'Score',  width: 90 },
];

export default function DevDocsPage() {
  const [activeId, setActiveId] = useState('getting-started');
  useEffect(() => {
    const sections = NAV_ITEMS.flatMap((n) => [n.id, ...(n.children?.map((c) => c.id) ?? [])]);
    const observer = new IntersectionObserver(
      (entries) => { const visible = entries.find((e) => e.isIntersecting); if (visible) setActiveId(visible.target.id); },
      { threshold: 0.3, rootMargin: '-80px 0px -60% 0px' }
    );
    sections.forEach((id) => { const el = document.getElementById(id); if (el) observer.observe(el); });
    return () => observer.disconnect();
  }, []);

  return (
    <Box>
      {/* Page header */}
      <Box sx={{ mb: 4 }}>
        <Stack direction="row" spacing={1.5} alignItems="center" mb={1}>
          <Typography variant="h3" fontWeight={800}>Developer Docs</Typography>
          <Chip label="v5.0" color="primary" />
          <Chip label="FAANG-grade" color="success" size="small" />
        </Stack>
        <Typography variant="body1" color="text.secondary" sx={{ maxWidth: 640 }}>
          Complete reference for every component, hook, pattern, and API in this admin panel. Live previews, prop tables, and copy-ready code for everything.
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Sidebar */}
        <Grid size={{ xs: 12, md: 3, lg: 2.5 }}>
          <DocNavSidebar items={NAV_ITEMS} activeId={activeId} />
        </Grid>

        {/* Main content */}
        <Grid size={{ xs: 12, md: 9, lg: 9.5 }}>

          {/* Getting Started */}
          <DocSection id="getting-started" title="Getting Started" badge="Start here" badgeColor="success"
            description="Everything you need to run, understand, and extend this admin panel from day one.">
            <Alert severity="info" sx={{ mb: 3 }}>
              <strong>Quick start:</strong> unzip → npm install → npx msw init public/ --save → npm run dev
            </Alert>
            <DocCodeBlock language="bash" filename="Terminal" code={`unzip project_final_v5.zip && cd app
npm install
npx msw init public/ --save   # generates public/mockServiceWorker.js
npm run dev                    # → http://localhost:5173`} />

            <Typography variant="subtitle2" fontWeight={700} mb={1} mt={3}>Login credentials</Typography>
            <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
              {[['admin@demo.com','password','Admin — full access, can edit permissions'],['editor@demo.com','password','Editor — can create/edit, cannot delete'],['viewer@demo.com','password','Viewer — read-only on all resources']].map(([email, pwd, desc]) => (
                <Box key={email} sx={{ display: 'flex', gap: 2, mb: 1, flexWrap: 'wrap', alignItems: 'center' }}>
                  <Typography variant="caption" sx={{ fontFamily: 'monospace', fontWeight: 600, minWidth: 200 }}>{email}</Typography>
                  <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'text.secondary', minWidth: 80 }}>{pwd}</Typography>
                  <Typography variant="caption" color="text.secondary">{desc}</Typography>
                </Box>
              ))}
            </Paper>

            <Typography variant="subtitle2" fontWeight={700} mb={1}>Useful scripts</Typography>
            <DocCodeBlock language="bash" code={`npm run dev              # Start dev server with MSW
npm run build            # Production build
npm run build:analyze    # Build + open bundle treemap (stats.html)
npm run lint             # ESLint (zero warnings policy)
npm run test             # Vitest in watch mode
npm run test:run         # Vitest single run (CI)
npm run release          # Bump version + CHANGELOG + GitHub release
npm run generate-types   # openapi.yaml → src/shared/types/api.generated.ts`} />

            <Typography variant="subtitle2" fontWeight={700} mb={1} mt={3}>Add ?devtools=1 to URL</Typography>
            <Typography variant="body2" color="text.secondary">Append <code>?devtools=1</code> to any URL to open the React Query Devtools panel — inspect cache state, stale times, and refetch manually.</Typography>
          </DocSection>

          {/* Architecture */}
          <DocSection id="architecture" title="Architecture"
            description="Feature-based folder structure. Every feature is self-contained with its own api/, hooks/, pages/, and components/.">
            <DocCodeBlock language="text" filename="Project structure" code={`src/
├── app/providers/       # AppProviders — Theme, QueryClient, i18n, Idle timer
├── core/
│   ├── api/client.ts    # Axios + CSRF + token refresh queue
│   ├── auth/            # AuthStore (Zustand) + ProtectedRoute
│   ├── config/env.ts    # Env var validation at startup
│   ├── i18n/            # EN + FR translations (12 namespaces)
│   ├── router/          # Routes enum + AppRoutes
│   ├── sse/useSSE.ts    # SSE hook with polling fallback in dev
│   └── theme/           # light / blue / dark themes
├── features/            # One folder per business domain
│   ├── analytics/       # 6 charts: revenue, funnel, cohort, geo
│   ├── audit/           # Filterable audit log
│   ├── auth/            # Login, ForgotPassword, ResetPassword
│   ├── dashboard/       # Live stats, activity feed, signups chart
│   ├── devdocs/         # This documentation system
│   ├── invite/          # Email invite flow
│   ├── notifications/   # Bell + full notifications page
│   ├── profile/         # Edit, password, 2FA, sessions, GDPR
│   ├── reports/         # Scheduled exports
│   ├── search/          # Global ⌘K search with Fuse.js
│   ├── settings/        # General, Notifications, Database
│   ├── system/          # System health + service latency
│   └── users/           # Full CRUD, bulk actions, detail page
├── layout/              # MainLayout, Navbar, Sidebar, MobileNav
├── mocks/handlers/      # MSW handlers (one per feature)
├── shared/
│   ├── components/      # AdvancedDataTable, Can, EmptyState, skeletons
│   ├── hooks/           # usePermission, useFeatureFlag, useDebounce…
│   ├── lib/             # queryClient + STALE constants, sanitize
│   ├── stores/          # useAppStore, useUIStore, useTenantStore
│   └── types/           # roles.ts (permissions matrix)
└── test/__tests__/      # Vitest unit + API contract tests`} />

            <Typography variant="subtitle2" fontWeight={700} mb={1} mt={2}>Data flow</Typography>
            <DocCodeBlock language="text" code={`User action
  → Component (calls useMutation hook)
    → React Query (optimistic update → update cache immediately)
      → API function (usersApi.ts)
        → Axios client (adds Bearer token + CSRF header)
          → MSW intercepts in dev (usersHandlers.ts)
            → Response → React Query cache updated
              → Component re-renders with new data
                → On error: rollback optimistic update`} />
          </DocSection>

          {/* Components */}
          <DocSection id="components" title="Components"
            description="All shared components with live previews, prop tables, and usage examples.">

            {/* StatCard */}
            <Box id="comp-statcard" sx={{ mb: 5, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>StatCard</Typography>
              <Typography variant="body2" color="text.secondary" mb={2}>Dashboard metric card with icon, value, and hover elevation effect.</Typography>
              <DocLivePreview
                title="StatCard — all variants"
                preview={
                  <Grid container spacing={2} sx={{ width: '100%' }}>
                    <Grid size={{ xs: 12, sm: 6, md: 3 }}><StatCard title="Total Users" value={1245} icon={<PeopleIcon />} color="#1976d2" /></Grid>
                    <Grid size={{ xs: 12, sm: 6, md: 3 }}><StatCard title="Revenue" value="$12,340" icon={<AttachMoneyIcon />} color="#2e7d32" /></Grid>
                    <Grid size={{ xs: 12, sm: 6, md: 3 }}><StatCard title="Active" value={847} icon={<TrendingUpIcon />} color="#9c27b0" /></Grid>
                    <Grid size={{ xs: 12, sm: 6, md: 3 }}><StatCard title="No icon" value={320} /></Grid>
                  </Grid>
                }
                code={`import StatCard from '@/features/dashboard/components/StatCard';
import PeopleIcon from '@mui/icons-material/People';

<StatCard
  title="Total Users"
  value={1245}
  icon={<PeopleIcon />}
  color="#1976d2"
/>`} />
              <DocPropTable props={[
                { name: 'title', type: 'string', required: true, description: 'Label shown above the value.' },
                { name: 'value', type: 'string | number', required: true, description: 'The metric to display. Numbers are formatted with toLocaleString().' },
                { name: 'icon', type: 'ReactNode', description: 'MUI icon rendered inside the colored circle.' },
                { name: 'color', type: 'string', default: "'primary.main'", description: 'Background color of the icon bubble. Accepts any CSS color or MUI theme token.' },
              ]} />
            </Box>

            {/* AdvancedDataTable */}
            <Box id="comp-table" sx={{ mb: 5, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>AdvancedDataTable</Typography>
              <Typography variant="body2" color="text.secondary" mb={2}>Full-featured table with global search, column filters, sort, show/hide columns (persisted), CSV export, and PDF export. Pass any typed rows and column definitions.</Typography>
              <Alert severity="info" sx={{ mb: 2 }}>
                <strong>Live demo below</strong> — try searching "alice", adding a filter on Role = "admin", hiding a column, then exporting to CSV.
              </Alert>
              <AdvancedDataTable rows={DEMO_ROWS} columns={DEMO_COLS} title="Demo Users" exportFilename="demo-users" tableId="devdocs-demo" pageSize={5} />
              <DocCodeBlock language="tsx" filename="Usage" code={`import AdvancedDataTable from '@/shared/components/AdvancedDataTable/AdvancedDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';

interface User { id: number; name: string; email: string; role: string; }

const columns: ColumnDef<User>[] = [
  { field: 'name',  headerName: 'Name',  flex: 1 },
  { field: 'email', headerName: 'Email', flex: 1.5 },
  {
    field: 'role',
    headerName: 'Role',
    width: 120,
    renderCell: (row) => <Chip label={row.role} size="small" />,
    exportValue: (row) => row.role,  // what to write in CSV/PDF
  },
  {
    field: 'actions',
    headerName: '',
    sortable: false,     // exclude from sort
    filterable: false,   // exclude from column filters
    renderCell: (row) => <IconButton onClick={() => handleEdit(row)}><EditIcon /></IconButton>,
  },
];

<AdvancedDataTable
  rows={users}
  columns={columns}
  title="Users"
  exportFilename="users-export"
  tableId="users-table"    // persists column visibility to localStorage
  pageSize={10}
/>`} />
              <DocPropTable props={[
                { name: 'rows',           type: 'T[]',           required: true, description: 'Array of data objects. Generic — works with any shape.' },
                { name: 'columns',        type: 'ColumnDef<T>[]',required: true, description: 'Column definitions. See ColumnDef interface below.' },
                { name: 'title',          type: 'string',        default: "'Data'",   description: 'Shown in the toolbar and as the PDF heading.' },
                { name: 'exportFilename', type: 'string',        default: "'export'", description: 'Base filename for CSV and PDF downloads (without extension).' },
                { name: 'tableId',        type: 'string',        default: "'default'",description: 'localStorage key for persisting column visibility. Use a unique ID per table instance.' },
                { name: 'pageSize',       type: 'number',        default: '10',       description: 'Default rows per page.' },
              ]} />
              <Typography variant="caption" fontWeight={700} color="text.secondary" display="block" mb={1}>ColumnDef&lt;T&gt; options</Typography>
              <DocPropTable props={[
                { name: 'field',        type: 'keyof T',                        required: true, description: 'Key of the row object to display in this column.' },
                { name: 'headerName',   type: 'string',                         required: true, description: 'Column header label.' },
                { name: 'flex',         type: 'number',                         description: 'Flex-grow ratio. Use instead of width for responsive columns.' },
                { name: 'width',        type: 'number',                         description: 'Fixed pixel width.' },
                { name: 'sortable',     type: 'boolean',                        default: 'true',  description: 'Set false to disable sort on this column (e.g. for action columns).' },
                { name: 'filterable',   type: 'boolean',                        default: 'true',  description: 'Set false to hide from the column filter dropdown.' },
                { name: 'renderCell',   type: '(row: T) => ReactNode',          description: 'Custom cell renderer. Return any JSX.' },
                { name: 'exportValue',  type: '(row: T) => string',             description: 'What to write in CSV/PDF for this column. Defaults to String(row[field]).' },
              ]} />
            </Box>

            {/* Can */}
            <Box id="comp-can" sx={{ mb: 5, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>Can — RBAC Gate</Typography>
              <Typography variant="body2" color="text.secondary" mb={2}>Renders children only if the current user has the required permission. Uses the permissions matrix in <code>src/shared/types/roles.ts</code>.</Typography>
              <DocLivePreview
                title="Can — viewer vs admin"
                preview={
                  <Stack spacing={1}>
                    <Box sx={{ p: 1.5, border: '1px dashed', borderColor: 'divider', borderRadius: 1 }}>
                      <Typography variant="caption" color="text.secondary" display="block" mb={1}>Login as <strong>admin</strong> to see this button:</Typography>
                      <Can resource="user" action="delete" fallback={<Chip label="Hidden — no delete permission" size="small" color="error" variant="outlined" />}>
                        <Button variant="contained" color="error" size="small">Delete User (admin only)</Button>
                      </Can>
                    </Box>
                    <Box sx={{ p: 1.5, border: '1px dashed', borderColor: 'divider', borderRadius: 1 }}>
                      <Can resource="report" action="read">
                        <Chip label="Analytics visible to all roles" size="small" color="success" />
                      </Can>
                    </Box>
                  </Stack>
                }
                code={`import Can from '@/shared/components/Can';

// Show delete button only to users who can delete users
<Can resource="user" action="delete">
  <Button color="error">Delete</Button>
</Can>

// With fallback — show a message when access is denied
<Can
  resource="settings"
  action="edit"
  fallback={<Alert severity="error">Admin access required</Alert>}
>
  <SettingsForm />
</Can>

// Available resources: 'user' | 'role' | 'menu' | 'settings' | 'audit' | 'report'
// Available actions: 'read' | 'write' | 'edit' | 'delete' | 'approve'`} />
              <DocPropTable props={[
                { name: 'resource', type: 'Resource', required: true, description: "The resource to check. One of: 'users', 'users.roles', 'users.permissions', 'analytics', 'settings', 'settings.notifications', 'settings.database'." },
                { name: 'action',   type: 'Action',   required: true, description: "The action to check. One of: 'view', 'create', 'edit', 'delete'." },
                { name: 'children', type: 'ReactNode', required: true, description: 'Rendered when the user has permission.' },
                { name: 'fallback', type: 'ReactNode', default: 'null', description: 'Rendered when the user does NOT have permission. Defaults to rendering nothing.' },
              ]} />
            </Box>

            {/* EmptyState */}
            <Box id="comp-emptystate" sx={{ mb: 5, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>EmptyState</Typography>
              <Typography variant="body2" color="text.secondary" mb={2}>Centered empty-content placeholder with optional CTA button. Use instead of blank tables or lists.</Typography>
              <DocLivePreview
                title="EmptyState — with and without action"
                preview={
                  <Grid container spacing={2} sx={{ width: '100%' }}>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <Paper variant="outlined" sx={{ borderRadius: 2 }}>
                        <EmptyState icon={<PeopleIcon />} title="No users found" description="Try adjusting your search or filters." action={{ label: 'Add User', onClick: () => {} }} />
                      </Paper>
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <Paper variant="outlined" sx={{ borderRadius: 2 }}>
                        <EmptyState icon={<TrendingUpIcon />} title="No data yet" description="Data will appear once activity is recorded." />
                      </Paper>
                    </Grid>
                  </Grid>
                }
                code={`import EmptyState from '@/shared/components/EmptyState/EmptyState';
import PeopleIcon from '@mui/icons-material/People';

// With CTA button
<EmptyState
  icon={<PeopleIcon />}
  title="No users found"
  description="Try adjusting your search or filters."
  action={{ label: 'Add User', onClick: () => setDialogOpen(true) }}
/>

// Without button (display only)
<EmptyState
  icon={<TrendingUpIcon />}
  title="No data yet"
  description="Data will appear once activity is recorded."
/>`} />
              <DocPropTable props={[
                { name: 'icon',        type: 'ReactNode', required: true,  description: 'MUI icon rendered large and dimmed.' },
                { name: 'title',       type: 'string',    required: true,  description: 'Primary message.' },
                { name: 'description', type: 'string',                     description: 'Secondary explanation text. Max width 360px.' },
                { name: 'action',      type: '{ label: string; onClick: () => void }', description: 'Optional CTA button below the description.' },
              ]} />
            </Box>

            {/* ConfirmDialog */}
            <Box id="comp-confirm" sx={{ mb: 5, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>ConfirmDialog</Typography>
              <Typography variant="body2" color="text.secondary" mb={2}>Global confirmation modal driven by <code>useConfirmStore</code>. Call from anywhere — no need to add dialog state to your component.</Typography>
              <DocCodeBlock language="tsx" code={`import { useConfirmStore } from '@/shared/stores/useConfirmStore';

function MyComponent() {
  const { confirm } = useConfirmStore();

  const handleDelete = () => {
    confirm({
      title: 'Delete user',
      message: 'Remove Alice? This cannot be undone.',
      confirmLabel: 'Delete',
      severity: 'error',            // 'error' | 'warning' | 'info'
      onConfirm: () => deleteUser(id),
    });
  };

  return <Button onClick={handleDelete}>Delete</Button>;
}`} />
              <Alert severity="info">The ConfirmDialog is already mounted globally in AppProviders. Just call <code>confirm()</code> — no JSX to add to your component.</Alert>
            </Box>
          </DocSection>

          {/* Hooks */}
          <DocSection id="hooks" title="Hooks"
            description="Custom hooks for permissions, feature flags, debouncing, SSE, and more.">

            <Box id="hook-permission" sx={{ mb: 4, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={1}>usePermission</Typography>
              <DocCodeBlock language="tsx" code={`import { usePermission } from '@/shared/hooks/usePermission';

function MyComponent() {
  const canEdit    = usePermission('user', 'edit');
  const canDelete  = usePermission('user', 'delete');
  const canViewLog = usePermission('audit', 'read');

  return (
    <Box>
      {canEdit    && <Button>Edit</Button>}
      {canDelete  && <Button color="error">Delete</Button>}
      {canViewLog && <AuditLogPanel />}
    </Box>
  );
}
// Returns true/false, resolved from the user's Keycloak roles -> real
// Role/RolePermission/Permission grants on the backend`} />
            </Box>

            <Box id="hook-featureflag" sx={{ mb: 4, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>useFeatureFlag</Typography>
              <Typography variant="body2" color="text.secondary" mb={1}>Control feature rollout via VITE_FLAGS env var. Ship code dark, enable per environment.</Typography>
              <DocCodeBlock language="tsx" code={`import { useFeatureFlag } from '@/shared/hooks/useFeatureFlag';

function UsersPage() {
  const canBulkDelete = useFeatureFlag('bulk-delete');       // default: false
  const ssoEnabled    = useFeatureFlag('sso-login', true);   // default: true

  return (
    <Box>
      {canBulkDelete && <BulkActionBar />}
      {ssoEnabled    && <GoogleLoginButton />}
    </Box>
  );
}

// .env (or .env.staging / .env.production):
// VITE_FLAGS={"bulk-delete":true,"sso-login":true,"reports-page":false}`} />
            </Box>

            <Box id="hook-debounce" sx={{ mb: 4, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>useDebounce</Typography>
              <DocCodeBlock language="tsx" code={`import { useDebounce } from '@/shared/hooks/useDebounce';

function SearchInput() {
  const [query, setQuery] = useState('');
  const debouncedQuery = useDebounce(query, 300);   // 300ms delay

  // Only fires when user stops typing for 300ms
  const { data } = useQuery({
    queryKey: ['search', debouncedQuery],
    queryFn: () => searchUsers(debouncedQuery),
    enabled: debouncedQuery.length > 1,
  });

  return <TextField value={query} onChange={(e) => setQuery(e.target.value)} />;
}`} />
            </Box>

            <Box id="hook-pagetitle" sx={{ mb: 4, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>usePageTitle</Typography>
              <DocCodeBlock language="tsx" code={`import { usePageTitle } from '@/shared/hooks/usePageTitle';

// Auto-sets document.title based on current route
// Already wired in MainLayout — no action needed for standard routes

// Override for dynamic titles (e.g. user detail page):
function UserDetail() {
  const { data: user } = useUser(id);
  usePageTitle(user ? \`\${user.name}\` : 'User');
  // → document.title = "Alice Martin — Admin Panel"
}`} />
            </Box>

            <Box id="hook-sse" sx={{ mb: 4, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>useSSE</Typography>
              <Typography variant="body2" color="text.secondary" mb={1}>Server-Sent Events with automatic polling fallback in dev (MSW doesn't support SSE natively).</Typography>
              <DocCodeBlock language="tsx" code={`import { useSSE } from '@/core/sse/useSSE';
import { useQueryClient } from '@tanstack/react-query';

function LiveWidget() {
  const qc = useQueryClient();

  useSSE({
    url: '/api/dashboard/activity',
    enabled: isAuthenticated,
    onMessage: (data) => {
      // Update React Query cache directly — component re-renders automatically
      qc.setQueryData(['dashboard', 'activity'], data);
    },
  });

  // In dev: polls every 10s (MSW fallback)
  // In production: real SSE EventSource connection
}`} />
            </Box>

            <Box id="hook-offline" sx={{ mb: 4, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>useOfflineQueue</Typography>
              <DocCodeBlock language="tsx" code={`// Already wired in AppProviders — works automatically.
// When the user goes offline:
//   1. Shows a warning snackbar
//   2. Queues any failed mutations in localStorage
// When the user comes back online:
//   3. Replays all queued mutations in order
//   4. Invalidates all React Query caches
//   5. Shows "All queued actions synced" success toast

// To manually queue a mutation (e.g. in a custom API call):
import { queueMutation } from '@/shared/hooks/useOfflineQueue';

async function saveWithOfflineSupport(data: UserPayload) {
  try {
    await apiClient.post('/users', data);
  } catch (err) {
    if (!navigator.onLine) {
      queueMutation('/api/users', 'POST', data);
    } else {
      throw err;
    }
  }
}`} />
            </Box>
          </DocSection>

          {/* Patterns */}
          <DocSection id="patterns" title="Patterns"
            description="Reusable patterns for every common task. Copy, rename, done.">

            <Box id="pattern-crud" sx={{ mb: 5, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>Adding a new CRUD feature</Typography>
              <Typography variant="body2" color="text.secondary" mb={2}>Five files, always the same shape. Copy from the users feature and rename.</Typography>
              <DocCodeBlock language="tsx" filename="1. src/features/products/api/productsApi.ts" code={`import apiClient from '@/core/api/client';

export interface Product { id: number; name: string; price: number; category: string; }
export type ProductPayload = Omit<Product, 'id'>;

export const getProducts  = (page: number, limit = 10) =>
  apiClient.get<{ data: Product[]; total: number }>('/products', { params: { page, limit } }).then(r => r.data);
export const createProduct = (body: ProductPayload) =>
  apiClient.post<Product>('/products', body).then(r => r.data);
export const updateProduct = (id: number, body: Partial<ProductPayload>) =>
  apiClient.put<Product>(\`/products/\${id}\`, body).then(r => r.data);
export const deleteProduct = (id: number) =>
  apiClient.delete(\`/products/\${id}\`).then(r => r.data);`} />
              <DocCodeBlock language="tsx" filename="2. src/features/products/hooks/useProducts.ts" code={`import { useQuery, useMutation } from '@tanstack/react-query';
import { getProducts, createProduct, updateProduct, deleteProduct, type ProductPayload } from '../api/productsApi';
import { queryClient, STALE } from '@/shared/lib/queryClient';
import { useAppStore } from '@/shared/stores/useAppStore';

const KEY = 'products';

export function useProducts(page: number) {
  return useQuery({ queryKey: [KEY, page], queryFn: () => getProducts(page), staleTime: STALE.SHORT });
}

export function useCreateProduct() {
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: (body: ProductPayload) => createProduct(body),
    onMutate: async (newItem) => {
      await queryClient.cancelQueries({ queryKey: [KEY] });
      const prev = queryClient.getQueriesData({ queryKey: [KEY] });
      // Optimistic update here
      return { prev };
    },
    onError: (_e, _v, ctx) => ctx?.prev.forEach(([key, val]) => queryClient.setQueryData(key, val)),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: [KEY] }); notify('Product created', 'success'); },
  });
}

export function useDeleteProduct() {
  const { notify } = useAppStore();
  return useMutation({
    mutationFn: deleteProduct,
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: [KEY] }); notify('Product deleted', 'success'); },
  });
}`} />
              <DocCodeBlock language="tsx" filename="3. src/mocks/handlers/productsHandlers.ts" code={`import { http, HttpResponse } from 'msw';

const db = [{ id:1, name:'Widget A', price:29.99, category:'hardware' }];
let nextId = 2;

export const productsHandlers = [
  http.get('/api/products', ({ request }) => {
    const url = new URL(request.url);
    const page = Number(url.searchParams.get('page') ?? 1);
    const limit = Number(url.searchParams.get('limit') ?? 10);
    return HttpResponse.json({ data: db.slice((page-1)*limit, page*limit), total: db.length });
  }),
  http.post('/api/products', async ({ request }) => {
    const body = await request.json();
    const item = { id: nextId++, ...body };
    db.push(item);
    return HttpResponse.json(item, { status: 201 });
  }),
  http.put('/api/products/:id', async ({ params, request }) => {
    const idx = db.findIndex(p => p.id === Number(params.id));
    if (idx === -1) return HttpResponse.json({ message: 'Not found' }, { status: 404 });
    db[idx] = { ...db[idx], ...await request.json() };
    return HttpResponse.json(db[idx]);
  }),
  http.delete('/api/products/:id', ({ params }) => {
    const idx = db.findIndex(p => p.id === Number(params.id));
    if (idx !== -1) db.splice(idx, 1);
    return HttpResponse.json({ success: true });
  }),
];`} />
              <DocCodeBlock language="tsx" filename="4. Register in src/mocks/browser.ts" code={`import { productsHandlers } from './handlers/productsHandlers';
export const worker = setupWorker(...productsHandlers, ...otherHandlers);`} />
              <DocCodeBlock language="tsx" filename="5. Add route + menu item" code={`// src/core/router/routes.ts
PRODUCTS: '/products',

// src/layout/config/menuConfig.tsx
{ label: 'Products', icon: <InventoryIcon />, path: ROUTES.PRODUCTS },

// src/core/router/AppRoutes.tsx
<Route path={ROUTES.PRODUCTS} element={<Protected><Products /></Protected>} />`} />
            </Box>

            <Box id="pattern-optimistic" sx={{ mb: 5, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>Optimistic Updates</Typography>
              <Typography variant="body2" color="text.secondary" mb={2}>Update UI instantly, roll back on error. Pattern used in all user mutations.</Typography>
              <DocCodeBlock language="tsx" code={`useMutation({
  mutationFn: (id: number) => deleteUser(id),

  // 1. Called BEFORE the API request — update cache immediately
  onMutate: async (id) => {
    // Cancel any in-flight fetches so they don't overwrite our optimistic update
    await queryClient.cancelQueries({ queryKey: ['users'] });

    // Snapshot the previous value for rollback
    const prev = queryClient.getQueriesData<UsersPage>({ queryKey: ['users'] });

    // Optimistically update the cache
    queryClient.setQueriesData<UsersPage>({ queryKey: ['users'] }, (old) =>
      old ? { ...old, data: old.data.filter(u => u.id !== id), total: old.total - 1 } : old
    );

    return { prev };   // returned as context
  },

  // 2. Called on API error — restore previous cache
  onError: (_err, _id, ctx) => {
    ctx?.prev.forEach(([key, val]) => queryClient.setQueryData(key, val));
  },

  // 3. Called on success — sync with server truth
  onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
})`} />
            </Box>

            <Box id="pattern-stale" sx={{ mb: 5, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>Cache Strategy</Typography>
              <Typography variant="body2" color="text.secondary" mb={2}>Use the right stale time for each data type. Import STALE constants instead of hardcoding milliseconds.</Typography>
              <DocCodeBlock language="tsx" code={`import { STALE } from '@/shared/lib/queryClient';

// STALE.REALTIME  = 15s  — dashboard stats, notifications, activity feed
// STALE.SHORT     = 2min — user lists, audit log (changes frequently)
// STALE.MEDIUM    = 5min — analytics, settings (default for all queries)
// STALE.LONG      = 30min — roles/permissions (rarely changes)
// STALE.STATIC    = Infinity — feature flags, locale data (never re-fetch)

useQuery({
  queryKey: ['dashboard', 'stats'],
  queryFn: getDashboardStats,
  staleTime: STALE.REALTIME,
  refetchInterval: STALE.REALTIME,  // also auto-polls every 15s
});

useQuery({
  queryKey: ['roles', 'permissions'],
  queryFn: getRolesPermissions,
  staleTime: STALE.LONG,            // won't re-fetch for 30 minutes
});`} />
            </Box>

            <Box id="pattern-auth" sx={{ mb: 5, scrollMarginTop: 80 }}>
              <Typography variant="h6" fontWeight={700} mb={0.5}>Protected Routes</Typography>
              <DocCodeBlock language="tsx" code={`// Basic auth guard (redirects to /login if not authenticated)
<Route path="/users" element={
  <ProtectedRoute>
    <MainLayout><Users /></MainLayout>
  </ProtectedRoute>
} />

// Resource-level guard (redirects to /403 if no permission)
<ProtectedRoute resource="settings" action="edit">
  <Settings />
</ProtectedRoute>

// Programmatic check in component logic
import { usePermission } from '@/shared/hooks/usePermission';

const canExport = usePermission('report', 'read');
const handleExport = () => {
  if (!canExport) return;
  // ...
};`} />
            </Box>
          </DocSection>

          {/* Stores */}
          <DocSection id="stores" title="Stores (Zustand)"
            description="Four global stores. All persisted to localStorage except accessToken (memory only).">
            <DocCodeBlock language="tsx" code={`// useAuthStore — current user, login/logout, rate limiting
import { useAuthStore } from '@/core/auth/stores/useAuthStore';
const { user, isAuthenticated, login, logout } = useAuthStore();
// user: { id, name, email, role } | null
// accessToken: NOT in localStorage (security — lives in memory only)

// useUIStore — theme, language, sidebar, recent search pages, onboarding
import { useUIStore } from '@/shared/stores/useUIStore';
const { theme, language, sidebarOpen, seenOnboarding, toggleTheme, setLanguage, setSeenOnboarding } = useUIStore();

// useAppStore — global loading bar, snackbar notifications
import { useAppStore } from '@/shared/stores/useAppStore';
const { notify, setLoading } = useAppStore();
notify('User saved', 'success');      // opens snackbar
notify('Deleted. Click Undo.', 'info'); // shows Undo button for 5s
setLoading(true);                      // shows top loading bar

// useConfirmStore — global confirm dialog (call from anywhere)
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
const { confirm } = useConfirmStore();
confirm({ title:'Delete?', message:'Cannot be undone.', severity:'error', onConfirm: doDelete });`} />
          </DocSection>

          {/* Api Layer */}
          <DocSection id="api-layer" title="API Layer"
            description="How requests flow from component to MSW and back.">
            <DocCodeBlock language="tsx" code={`// Every feature has its own api/ file.
// Direct HTTP → never call apiClient from a component, always go through a hook.

// src/core/api/client.ts — Axios instance with:
//   withCredentials: true          (sends cookies)
//   X-CSRF-Token header            (on every mutation)
//   X-Tenant-ID header             (multi-tenancy)
//   Bearer token (dev only)        (MSW can't use httpOnly cookies)
//   401 retry queue                (waits for token refresh, replays)

// To connect to a real backend instead of MSW:
// 1. Set VITE_API_URL=https://your-api.com in .env
// 2. Remove or comment out the MSW initialization block in main.tsx:
//    if (import.meta.env.DEV) { const { worker } = await import('./mocks/browser')... }
// 3. Your real backend must return the same JSON shapes as the MSW handlers

// To add a new endpoint to MSW:
// src/mocks/handlers/yourHandlers.ts → add http.get/post/put/delete
// src/mocks/browser.ts → spread ...yourHandlers into setupWorker()`} />
          </DocSection>

          {/* I18n */}
          <DocSection id="i18n" title="i18n"
            description="Full EN + FR translations. 12 namespaces covering every page.">
            <DocCodeBlock language="tsx" code={`import { useTranslation } from 'react-i18next';

function MyPage() {
  const { t } = useTranslation();

  return (
    <Box>
      <Typography>{t('users.title')}</Typography>
      {/* With interpolation: */}
      <Typography>{t('users.deleteConfirmMessage', { name: user.name })}</Typography>
    </Box>
  );
}

// Add new keys: src/core/i18n/locales/en.ts AND fr.ts
// Namespaces: menu | dashboard | users | auth | settings
//             analytics | audit | notifications | profile
//             system | reports | invite | errors | common

// Language switching (persisted to useUIStore):
const { i18n } = useTranslation();
const { setLanguage } = useUIStore();
const switchToFrench = () => { setLanguage('fr'); i18n.changeLanguage('fr'); };`} />
          </DocSection>

          {/* Testing */}
          <DocSection id="testing" title="Testing"
            description="Vitest unit tests + Playwright E2E + API contract validation.">
            <DocCodeBlock language="bash" code={`npm run test             # Vitest watch mode
npm run test:run         # Single run (CI)
npm run test:coverage    # With coverage report
npx playwright test      # E2E (requires dev server on :5173)`} />
            <DocCodeBlock language="tsx" filename="Unit test pattern (Vitest)" code={`import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useAppStore } from '@/shared/stores/useAppStore';

describe('useAppStore', () => {
  beforeEach(() => useAppStore.setState({ loading: false, snackbar: { open: false, message: '', severity: 'info' } }));

  it('notify opens snackbar', () => {
    useAppStore.getState().notify('Hello', 'success');
    expect(useAppStore.getState().snackbar).toMatchObject({ open: true, message: 'Hello', severity: 'success' });
  });
});`} />
            <DocCodeBlock language="tsx" filename="API contract test (Zod)" code={`import { z } from 'zod';

// Define the schema once — catches shape mismatches before they reach production
const userSchema = z.object({
  id: z.number(),
  name: z.string().min(1),
  email: z.string().email(),
  role: z.enum(['admin', 'editor', 'viewer']),
});

it('user response matches schema', () => {
  const apiResponse = { id: 1, name: 'Alice', email: 'alice@demo.com', role: 'admin' };
  expect(() => userSchema.parse(apiResponse)).not.toThrow();
});`} />
          </DocSection>

          {/* Env */}
          <DocSection id="env" title="Environment Variables"
            description="All vars validated at startup — missing required vars crash with a clear message in production.">
            <DocCodeBlock language="bash" filename=".env (development)" code={`VITE_API_URL=/api                              # API base URL (/api → MSW in dev)
VITE_APP_TITLE=Admin Panel                    # document.title suffix
VITE_IDLE_TIMEOUT_MS=900000                   # 15 min idle → session warning
VITE_SESSION_WARNING_MS=60000                 # 60s countdown before logout
VITE_LOGIN_MAX_ATTEMPTS=5                     # Failed attempts before lockout
VITE_FLAGS={"bulk-delete":true,"sso-login":true}  # Feature flags (JSON)`} />
            <DocCodeBlock language="bash" filename=".env.production" code={`VITE_API_URL=https://api.yourapp.com
VITE_APP_TITLE=Admin Panel
VITE_IDLE_TIMEOUT_MS=900000
VITE_SESSION_WARNING_MS=60000
VITE_LOGIN_MAX_ATTEMPTS=5
VITE_FLAGS={"bulk-delete":true,"sso-login":true}
# VITE_SENTRY_DSN=https://xxx@o0.ingest.sentry.io/0`} />
            <Alert severity="warning">All <code>VITE_*</code> vars are embedded in the JS bundle at build time. Never put secrets here — only public configuration.</Alert>
          </DocSection>

        </Grid>
      </Grid>
    </Box>
  );
}
