import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import SettingsIcon from '@mui/icons-material/Settings';
import ShieldIcon from '@mui/icons-material/Shield';
import TuneIcon from '@mui/icons-material/Tune';
import NotificationsIcon from '@mui/icons-material/Notifications';
import StorageIcon from '@mui/icons-material/Storage';
import GroupIcon from '@mui/icons-material/Group';
import ApartmentIcon from '@mui/icons-material/Apartment';
import AccountTreeIcon from '@mui/icons-material/AccountTree';
import Groups2Icon from '@mui/icons-material/Groups2';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import AssignmentIcon from '@mui/icons-material/Assignment';
import PersonIcon from '@mui/icons-material/Person';
import TableChartIcon from '@mui/icons-material/TableChart';
import VpnKeyIcon from '@mui/icons-material/VpnKey';
import DevicesIcon from '@mui/icons-material/Devices';
import HistoryIcon from '@mui/icons-material/History';
import BugReportIcon from '@mui/icons-material/BugReport';
import EventNoteIcon from '@mui/icons-material/EventNote';
import GppMaybeIcon from '@mui/icons-material/GppMaybe';
import LabelIcon from '@mui/icons-material/Label';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import ForumIcon from '@mui/icons-material/Forum';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import DescriptionIcon from '@mui/icons-material/Description';
import GavelIcon from '@mui/icons-material/Gavel';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import LocalOfferOutlinedIcon from '@mui/icons-material/LocalOfferOutlined';
import ArticleIcon from '@mui/icons-material/Article';
import BorderColorIcon from '@mui/icons-material/BorderColor';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import SavingsIcon from '@mui/icons-material/Savings';
import PaymentsIcon from '@mui/icons-material/Payments';
import { useTranslation } from 'react-i18next';
import { ROUTES, GUIDE_ROUTE } from '@/core/router/routes';
import type { ReactNode } from 'react';
export interface MenuItem { label: string; icon?: ReactNode; path?: string; children?: MenuItem[]; dividerBefore?: boolean; }
export function useMenuConfig(): MenuItem[] {
  const { t } = useTranslation();
  return [
    { label: t('menu.dashboard'), icon: <DashboardIcon />, path: ROUTES.HOME },
    { label: t('menu.users'), icon: <PeopleIcon />, children: [
      { label: t('menu.allUsers'), icon: <GroupIcon />, path: ROUTES.USERS },
      { label: t('menu.roles'), icon: <ShieldIcon />, path: ROUTES.USERS_ROLES },
      { label: 'Role Permissions', icon: <ShieldIcon />, path: ROUTES.ROLE_PERMISSIONS },
      { label: t('menu.permissions'), icon: <ShieldIcon />, path: ROUTES.USERS_PERMISSIONS },
    ]},
    { label: 'Organization', icon: <ApartmentIcon />, children: [
      { label: 'Organizations', icon: <ApartmentIcon />, path: ROUTES.ORGANIZATIONS },
      { label: 'Departments', icon: <AccountTreeIcon />, path: ROUTES.DEPARTMENTS },
      { label: 'Teams', icon: <Groups2Icon />, path: ROUTES.TEAMS },
    ]},
    { label: 'Customers', icon: <PersonIcon />, path: ROUTES.CUSTOMERS },
    { label: 'Loan Products', icon: <AttachMoneyIcon />, path: ROUTES.LOAN_PRODUCTS },
    { label: 'Loan Applications', icon: <DescriptionIcon />, path: ROUTES.LOAN_APPLICATIONS },
    { label: 'Underwriting', icon: <GavelIcon />, path: ROUTES.UNDERWRITING_CASES },
    { label: 'Loan Approvals', icon: <ThumbUpIcon />, path: ROUTES.LOAN_APPROVALS },
    { label: 'Loan Offers', icon: <LocalOfferOutlinedIcon />, path: ROUTES.LOAN_OFFERS },
    { label: 'Loan Contracts', icon: <ArticleIcon />, path: ROUTES.LOAN_CONTRACTS },
    { label: 'Contract Signatures', icon: <BorderColorIcon />, path: ROUTES.CONTRACT_SIGNATURES },
    { label: 'Loan Disbursements', icon: <AccountBalanceIcon />, path: ROUTES.LOAN_DISBURSEMENTS },
    { label: 'Loan Accounts', icon: <SavingsIcon />, path: ROUTES.LOAN_ACCOUNTS },
    { label: 'Loan Repayments', icon: <PaymentsIcon />, path: ROUTES.LOAN_REPAYMENTS },
    { label: t('menu.settings'), icon: <SettingsIcon />, children: [
      { label: t('menu.general'), icon: <TuneIcon />, path: ROUTES.SETTINGS },
      { label: 'App Modules', icon: <StorageIcon />, path: ROUTES.APP_MODULES },
      { label: 'Menus', icon: <TuneIcon />, path: ROUTES.MENUS },
      { label: 'Menu Permissions', icon: <ShieldIcon />, path: ROUTES.MENU_PERMISSIONS },
      { label: 'Role Menu Access', icon: <ShieldIcon />, path: ROUTES.ROLE_MENUS },
      { label: 'Feature Flags', icon: <NotificationsIcon />, path: ROUTES.FEATURE_FLAGS },
      { label: 'Metadata', icon: <StorageIcon />, path: ROUTES.METADATA },
    ]},
    { label: 'Security & Audit', icon: <GppMaybeIcon />, dividerBefore: true, children: [
      { label: 'Audit Log', icon: <AssignmentIcon />, path: ROUTES.AUDIT },
      { label: 'Auth Logs', icon: <HistoryIcon />, path: ROUTES.AUTH_LOGS },
      { label: 'Login History', icon: <HistoryIcon />, path: ROUTES.LOGIN_HISTORY },
      { label: 'Sessions', icon: <DevicesIcon />, path: ROUTES.SESSIONS },
      { label: 'API Keys', icon: <VpnKeyIcon />, path: ROUTES.API_KEYS },
      { label: 'Error Logs', icon: <BugReportIcon />, path: ROUTES.ERROR_LOGS },
      { label: 'System Events', icon: <EventNoteIcon />, path: ROUTES.SYSTEM_EVENTS },
    ]},
    { label: 'Content', icon: <ForumIcon />, children: [
      { label: 'Notification Templates', icon: <NotificationsIcon />, path: ROUTES.NOTIFICATION_TEMPLATES },
      { label: 'Tags', icon: <LabelIcon />, path: ROUTES.TAGS },
      { label: 'Entity Tags', icon: <LocalOfferIcon />, path: ROUTES.ENTITY_TAGS },
    ]},
    { label: 'Advanced Table Demo', icon: <TableChartIcon />, path: ROUTES.TABLE_DEMO },
    { label: 'Profile', icon: <PersonIcon />, path: ROUTES.PROFILE },
    { label: 'Dev Guide', icon: <MenuBookIcon />, path: GUIDE_ROUTE },
    { label: 'Dev Docs', icon: <MenuBookIcon />, path: '/docs' },
    { label: 'UI Library', icon: <MenuBookIcon />, path: '/ui-docs' },
  ];
}
