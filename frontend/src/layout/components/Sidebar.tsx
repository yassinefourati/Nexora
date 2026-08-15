import { useEffect, useState, useCallback } from 'react';
import { Box, Drawer, Divider, List, Typography } from '@mui/material';
import { useLocation } from 'react-router-dom';
import { useMenuConfig, type MenuItem } from '../config/menuConfig';
import { useUIStore } from '@/shared/stores/useUIStore';
import { useResponsive } from '@/shared/hooks/useResponsive';
import SidebarMenuItem, { isPathActive, type OpenMap } from './SidebarMenuItem';

const DRAWER_WIDTH = 240; const COLLAPSED_WIDTH = 68;
const ROOT = 'root';

// only the branch containing the active route stays open, siblings collapse
function activeOpenMap(items: MenuItem[], pathname: string, parentKey = ROOT, map: OpenMap = {}): OpenMap {
  for (const item of items) {
    if (item.children?.length && isPathActive(item, pathname)) {
      map[parentKey] = item.label;
      activeOpenMap(item.children, pathname, item.label, map);
    }
  }
  return map;
}

export default function Sidebar() {
  const { sidebarOpen, toggleSidebar } = useUIStore();
  const { isMobile } = useResponsive();
  const menuConfig = useMenuConfig();
  const { pathname } = useLocation();
  const drawerWidth = sidebarOpen ? DRAWER_WIDTH : COLLAPSED_WIDTH;

  const [openMap, setOpenMap] = useState<OpenMap>(() => activeOpenMap(menuConfig, pathname));
  useEffect(() => { setOpenMap(activeOpenMap(menuConfig, pathname)); }, [pathname]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleToggle = useCallback((parentKey: string, label: string) => {
    setOpenMap((prev) => ({ ...prev, [parentKey]: prev[parentKey] === label ? null : label }));
  }, []);

  const content = (
    <Box component="nav" aria-label="Main navigation" sx={{ display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>
      <Box sx={{ height: 64, display: 'flex', alignItems: 'center', px: 2, flexShrink: 0 }}>
        <Typography variant="subtitle1" fontWeight={700} color="primary" noWrap>{sidebarOpen ? 'Admin Panel' : 'AP'}</Typography>
      </Box>
      <Divider />
      <Box sx={{ flexGrow: 1, overflowY: 'auto', overflowX: 'hidden', pt: 1 }}>
        <List disablePadding>
          {menuConfig.map((item) => (
            <SidebarMenuItem key={item.label} item={item} sidebarOpen={sidebarOpen} parentKey={ROOT} openMap={openMap} onToggle={handleToggle} />
          ))}
        </List>
      </Box>
    </Box>
  );
  if (isMobile) return <Drawer variant="temporary" open={sidebarOpen} onClose={toggleSidebar} ModalProps={{ keepMounted: true }} sx={{ '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' } }}>{content}</Drawer>;
  return <Drawer variant="permanent" sx={{ width: drawerWidth, flexShrink: 0, transition: 'width 0.3s', '& .MuiDrawer-paper': { width: drawerWidth, boxSizing: 'border-box', overflowX: 'hidden', transition: 'width 0.3s' } }}>{content}</Drawer>;
}
