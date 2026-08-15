import { BottomNavigation, BottomNavigationAction, Paper } from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import AssignmentIcon from '@mui/icons-material/Assignment';
import SettingsIcon from '@mui/icons-material/Settings';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@/core/router/routes';

const TABS = [
  { label: 'Dashboard', icon: <DashboardIcon />,  path: ROUTES.HOME },
  { label: 'Users',     icon: <PeopleIcon />,     path: ROUTES.USERS },
  { label: 'Audit Log', icon: <AssignmentIcon />, path: ROUTES.AUDIT },
  { label: 'Settings',  icon: <SettingsIcon />,   path: ROUTES.SETTINGS },
];

export default function MobileBottomNav() {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const current = TABS.findIndex((t) => t.path === pathname || (t.path !== '/' && pathname.startsWith(t.path)));

  return (
    <Paper sx={{ position: 'fixed', bottom: 0, left: 0, right: 0, zIndex: 1200, display: { sm: 'none' } }} elevation={8}>
      <BottomNavigation value={current === -1 ? false : current} onChange={(_, v) => navigate(TABS[v].path)} showLabels>
        {TABS.map((tab) => (
          <BottomNavigationAction key={tab.label} label={tab.label} icon={tab.icon} />
        ))}
      </BottomNavigation>
    </Paper>
  );
}
