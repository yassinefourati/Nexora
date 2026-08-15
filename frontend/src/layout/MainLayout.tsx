import { Box } from '@mui/material';
import { useResponsive } from '@/shared/hooks/useResponsive';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import Breadcrumbs from './components/Breadcrumbs';
import MobileBottomNav from './components/MobileBottomNav';
import { usePageTitle } from '@/shared/hooks/usePageTitle';
import PageTransitionBar from '@/shared/components/PageTransitionBar';
import type { ReactNode } from 'react';
export default function MainLayout({ children }: { children: ReactNode }) {
  const { isMobile } = useResponsive();
  usePageTitle();
  return (
    <Box sx={{ display: 'flex' }}>
      <PageTransitionBar />
      <Navbar /><Sidebar />
      <Box component="main" sx={{ flexGrow: 1, p: { xs: 2, md: 3 }, mt: 8, minWidth: 0, ...(isMobile && { ml: '0 !important' }) }}>
        <Breadcrumbs />{children}
      </Box>
      <MobileBottomNav />
    </Box>
  );
}
