import { Breadcrumbs as MuiBreadcrumbs, Typography, Link, Box } from '@mui/material';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';
import { useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ROUTES, GUIDE_ROUTE } from '@/core/router/routes';

function fmt(s: string) { return s.replace(/-/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase()); }

// intermediate crumbs that aren't a real page (e.g. "/analytics/reports") render as plain text, not a dead link
const KNOWN_PATHS = [...Object.values(ROUTES), GUIDE_ROUTE, '/docs', '/ui-docs'];
const KNOWN_PATTERNS = KNOWN_PATHS.map((p) => new RegExp('^' + p.replace(/:[^/]+/g, '[^/]+') + '$'));
function isKnownRoute(path: string) { return KNOWN_PATTERNS.some((re) => re.test(path)); }

export default function Breadcrumbs() {
  const { pathname } = useLocation(); const navigate = useNavigate(); const { t } = useTranslation();
  const segments = pathname.split('/').filter(Boolean);
  if (!segments.length) return null;
  const crumbs = segments.map((seg, i) => ({ label: fmt(seg), path: '/' + segments.slice(0, i+1).join('/') }));
  return (
    <Box sx={{ mb: 2 }}>
      <MuiBreadcrumbs separator={<NavigateNextIcon fontSize="small" />}>
        <Link underline="hover" color="inherit" sx={{ cursor: 'pointer' }} onClick={() => navigate(ROUTES.HOME)}>{t('common.home')}</Link>
        {crumbs.map((crumb, i) => (i === crumbs.length - 1 || !isKnownRoute(crumb.path))
          ? <Typography key={crumb.path} color={i === crumbs.length - 1 ? 'text.primary' : 'text.secondary'} fontWeight={i === crumbs.length - 1 ? 600 : 400}>{crumb.label}</Typography>
          : <Link key={crumb.path} underline="hover" color="inherit" sx={{ cursor: 'pointer' }} onClick={() => navigate(crumb.path)}>{crumb.label}</Link>)}
      </MuiBreadcrumbs>
    </Box>
  );
}
