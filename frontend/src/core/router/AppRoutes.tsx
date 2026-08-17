import { lazy, Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import { LinearProgress } from '@mui/material';
import { ROUTES, GUIDE_ROUTE } from './routes';
import ProtectedRoute from '@/core/auth/guards/ProtectedRoute';
import MainLayout from '@/layout/MainLayout';
import PageErrorBoundary from '@/shared/components/PageErrorBoundary';

// All page imports are lazy — each route becomes its own JS chunk.
// Heavy libraries (recharts, jspdf, @mui/x-data-grid) only load when needed.
const Login              = lazy(() => import('@/features/auth/pages/Login'));
const AuthCallback       = lazy(() => import('@/features/auth/pages/AuthCallback'));
const Dashboard          = lazy(() => import('@/features/dashboard/pages/Dashboard'));
const Users              = lazy(() => import('@/features/users/pages/Users'));
const UserDetail         = lazy(() => import('@/features/users/pages/UserDetail'));
const Roles              = lazy(() => import('@/features/roles/pages/Roles'));
const Permissions        = lazy(() => import('@/features/permissions/pages/Permissions'));
const RolePermissionsMatrix = lazy(() => import('@/features/permissions/pages/RolePermissionsMatrix'));
const Organizations      = lazy(() => import('@/features/organizations/pages/Organizations'));
const LoanProducts       = lazy(() => import('@/features/loanProducts/pages/LoanProducts'));
const Departments        = lazy(() => import('@/features/departments/pages/Departments'));
const Teams              = lazy(() => import('@/features/teams/pages/Teams'));
const Settings           = lazy(() => import('@/features/settings/pages/Settings'));
const AppModules         = lazy(() => import('@/features/appModules/pages/AppModules'));
const Menus              = lazy(() => import('@/features/menus/pages/Menus'));
const FeatureFlags       = lazy(() => import('@/features/featureFlags/pages/FeatureFlags'));
const MenuPermissions    = lazy(() => import('@/features/menuPermissions/pages/MenuPermissions'));
const RoleMenus          = lazy(() => import('@/features/roleMenus/pages/RoleMenus'));
const Metadata           = lazy(() => import('@/features/metadata/pages/Metadata'));
const AuthLogs           = lazy(() => import('@/features/authLogs/pages/AuthLogs'));
const LoginHistory       = lazy(() => import('@/features/loginHistory/pages/LoginHistory'));
const Sessions           = lazy(() => import('@/features/sessions/pages/Sessions'));
const ApiKeys            = lazy(() => import('@/features/apiKeys/pages/ApiKeys'));
const ErrorLogs          = lazy(() => import('@/features/errorLogs/pages/ErrorLogs'));
const SystemEvents       = lazy(() => import('@/features/systemEvents/pages/SystemEvents'));
const NotificationTemplates = lazy(() => import('@/features/notificationTemplates/pages/NotificationTemplates'));
const Tags               = lazy(() => import('@/features/tags/pages/Tags'));
const EntityTags         = lazy(() => import('@/features/entityTags/pages/EntityTags'));
const Profile            = lazy(() => import('@/features/profile/pages/Profile'));
const AuditLog           = lazy(() => import('@/features/audit/pages/AuditLog'));
const NotificationsPage  = lazy(() => import('@/features/notifications/pages/NotificationsPage'));
const SystemHealth       = lazy(() => import('@/features/system/pages/SystemHealth'));
const DeveloperGuide     = lazy(() => import('@/features/guide/pages/DeveloperGuide'));
const DevDocsPage        = lazy(() => import('@/features/devdocs/pages/DevDocsPage'));
const UiDocsPage         = lazy(() => import('@/features/uidocs/pages/UiDocsPage'));
const AdvancedTableDemo  = lazy(() => import('@/features/tabledemo/pages/AdvancedTableDemo'));
const NotFound           = lazy(() => import('@/shared/pages/NotFound'));
const Unauthorized       = lazy(() => import('@/shared/pages/Unauthorized'));

function RouteSpinner() {
  return <LinearProgress sx={{ position: 'fixed', top: 0, left: 0, right: 0, zIndex: 9999 }} />;
}

function Protected({ children }: { children: React.ReactNode }) {
  return (
    <ProtectedRoute>
      <MainLayout>
        <PageErrorBoundary>{children}</PageErrorBoundary>
      </MainLayout>
    </ProtectedRoute>
  );
}

export default function AppRoutes() {
  return (
    <Suspense fallback={<RouteSpinner />}>
      <Routes>
        <Route path={ROUTES.LOGIN}         element={<Login />} />
        <Route path={ROUTES.AUTH_CALLBACK} element={<AuthCallback />} />

        <Route path={ROUTES.HOME}                          element={<Protected><Dashboard /></Protected>} />
        <Route path={ROUTES.PROFILE}                       element={<Protected><Profile /></Protected>} />
        <Route path={ROUTES.NOTIFICATIONS}                 element={<Protected><NotificationsPage /></Protected>} />
        <Route path={ROUTES.AUDIT}                         element={<Protected><AuditLog /></Protected>} />
        <Route path={ROUTES.SYSTEM}                        element={<Protected><SystemHealth /></Protected>} />
        <Route path={ROUTES.USERS}                         element={<Protected><Users /></Protected>} />
        <Route path={ROUTES.USERS_DETAIL}                  element={<Protected><UserDetail /></Protected>} />
        <Route path={ROUTES.USERS_ROLES}                   element={<Protected><Roles /></Protected>} />
        <Route path={ROUTES.USERS_PERMISSIONS}             element={<Protected><Permissions /></Protected>} />
        <Route path={ROUTES.ROLE_PERMISSIONS}              element={<Protected><RolePermissionsMatrix /></Protected>} />
        <Route path={ROUTES.ORGANIZATIONS}                 element={<Protected><Organizations /></Protected>} />
        <Route path={ROUTES.LOAN_PRODUCTS}                 element={<Protected><LoanProducts /></Protected>} />
        <Route path={ROUTES.DEPARTMENTS}                   element={<Protected><Departments /></Protected>} />
        <Route path={ROUTES.TEAMS}                         element={<Protected><Teams /></Protected>} />
        <Route path={ROUTES.SETTINGS}                      element={<Protected><Settings /></Protected>} />
        <Route path={ROUTES.APP_MODULES}                   element={<Protected><AppModules /></Protected>} />
        <Route path={ROUTES.MENUS}                         element={<Protected><Menus /></Protected>} />
        <Route path={ROUTES.FEATURE_FLAGS}                 element={<Protected><FeatureFlags /></Protected>} />
        <Route path={ROUTES.MENU_PERMISSIONS}              element={<Protected><MenuPermissions /></Protected>} />
        <Route path={ROUTES.ROLE_MENUS}                    element={<Protected><RoleMenus /></Protected>} />
        <Route path={ROUTES.METADATA}                      element={<Protected><Metadata /></Protected>} />
        <Route path={ROUTES.AUTH_LOGS}                     element={<Protected><AuthLogs /></Protected>} />
        <Route path={ROUTES.LOGIN_HISTORY}                 element={<Protected><LoginHistory /></Protected>} />
        <Route path={ROUTES.SESSIONS}                      element={<Protected><Sessions /></Protected>} />
        <Route path={ROUTES.API_KEYS}                      element={<Protected><ApiKeys /></Protected>} />
        <Route path={ROUTES.ERROR_LOGS}                    element={<Protected><ErrorLogs /></Protected>} />
        <Route path={ROUTES.SYSTEM_EVENTS}                 element={<Protected><SystemEvents /></Protected>} />
        <Route path={ROUTES.NOTIFICATION_TEMPLATES}        element={<Protected><NotificationTemplates /></Protected>} />
        <Route path={ROUTES.TAGS}                          element={<Protected><Tags /></Protected>} />
        <Route path={ROUTES.ENTITY_TAGS}                   element={<Protected><EntityTags /></Protected>} />
        <Route path={GUIDE_ROUTE}                          element={<Protected><DeveloperGuide /></Protected>} />
        <Route path='/docs'                                element={<Protected><DevDocsPage /></Protected>} />
        <Route path='/ui-docs'                             element={<Protected><UiDocsPage /></Protected>} />
        <Route path={ROUTES.TABLE_DEMO}                    element={<Protected><AdvancedTableDemo /></Protected>} />
        <Route path="/403"                                 element={<Unauthorized />} />
        <Route path="*"                                    element={<NotFound />} />
      </Routes>
    </Suspense>
  );
}
