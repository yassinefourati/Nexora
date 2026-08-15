import { Fragment } from 'react';
import { Collapse, List, ListItemButton, ListItemIcon, ListItemText, Popper, Paper, Fade, Box } from '@mui/material';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import CircleIcon from '@mui/icons-material/Circle';
import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import type { MenuItem } from '../config/menuConfig';

export type OpenMap = Record<string, string | null>;

interface Props {
  item: MenuItem;
  depth?: number;
  sidebarOpen: boolean;
  /** Key identifying this item's sibling group — only one child per parentKey stays open (accordion). */
  parentKey: string;
  openMap: OpenMap;
  onToggle: (parentKey: string, label: string) => void;
}

export function isPathActive(item: MenuItem, pathname: string): boolean {
  if (item.path === pathname) return true;
  return item.children?.some((c) => isPathActive(c, pathname)) ?? false;
}

export default function SidebarMenuItem({ item, depth = 0, sidebarOpen, parentKey, openMap, onToggle }: Props) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const hasChildren = Boolean(item.children?.length);
  const active = isPathActive(item, pathname);
  const isLeafActive = item.path === pathname;
  const open = openMap[parentKey] === item.label;
  const [hovered, setHovered] = useState(false);
  const [anchorEl, setAnchorEl] = useState<HTMLDivElement | null>(null);
  const icon = item.icon ?? <CircleIcon sx={{ fontSize: Math.max(6, 10 - depth * 2) }} />;
  const button = (
    <ListItemButton onClick={() => hasChildren ? onToggle(parentKey, item.label) : item.path && navigate(item.path)} selected={isLeafActive}
      sx={{ pl: sidebarOpen ? 2 + depth * 1.5 : 1.5, borderRadius: 1, mx: 0.5, mb: 0.25, minHeight: 40, transition: 'padding 0.3s',
        ...(isLeafActive && {
          '&.Mui-selected': { bgcolor: 'primary.main', color: 'primary.contrastText' },
          '&.Mui-selected:hover': { bgcolor: 'primary.main' },
          '&.Mui-selected .MuiListItemIcon-root': { color: 'primary.contrastText' },
          '&.Mui-selected .MuiListItemText-primary': { color: 'primary.contrastText', fontWeight: 700 },
        }),
        ...(active && !isLeafActive && { '& .MuiListItemText-primary': { fontWeight: 700 } }) }}>
      <ListItemIcon sx={{ minWidth: 36, color: isLeafActive ? 'inherit' : 'text.secondary' }}>{icon}</ListItemIcon>
      {sidebarOpen && (<><ListItemText primary={item.label} primaryTypographyProps={{ fontSize: depth === 0 ? '0.875rem' : '0.8125rem', fontWeight: active ? 600 : 400, noWrap: true }} />{hasChildren && (open ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />)}</>)}
    </ListItemButton>
  );
  return (
    <Fragment>
      {item.dividerBefore && sidebarOpen && <Box component="li" sx={{ my: 0.5, mx: 1, listStyle: 'none', borderTop: 1, borderColor: 'divider' }} />}
      <Box component="li" sx={{ listStyle: 'none' }}>
        <Box ref={setAnchorEl} onMouseEnter={() => setHovered(true)} onMouseLeave={() => setHovered(false)}>
          {button}
          {!sidebarOpen && depth === 0 && (
            <Popper open={hovered} anchorEl={anchorEl} placement="right-start" transition sx={{ zIndex: 1300 }}>
              {({ TransitionProps }) => (
                <Fade {...TransitionProps} timeout={150}>
                  <Paper elevation={4} sx={{ ml: 0.5, minWidth: 200, py: 0.5 }}>
                    <List disablePadding>
                      <ListItemButton disabled sx={{ py: 0.5, opacity: '1 !important' }}><ListItemText primary={item.label} primaryTypographyProps={{ fontWeight: 700, fontSize: '0.75rem', color: 'text.secondary' }} /></ListItemButton>
                      {hasChildren
                        ? (item.children ?? []).map((child) => (
                            <SidebarMenuItem key={child.label} item={child} depth={1} sidebarOpen parentKey={item.label} openMap={openMap} onToggle={onToggle} />
                          ))
                        : <ListItemButton selected={isLeafActive} onClick={() => item.path && navigate(item.path)}><ListItemText primary={item.label} /></ListItemButton>}
                    </List>
                  </Paper>
                </Fade>
              )}
            </Popper>
          )}
        </Box>
        {hasChildren && (
          <Collapse in={sidebarOpen && open} timeout="auto" unmountOnExit>
            <List disablePadding component="ul" sx={{ pl: 0 }}>
              {(item.children ?? []).map((child) => (
                <SidebarMenuItem key={child.label} item={child} depth={depth + 1} sidebarOpen={sidebarOpen} parentKey={item.label} openMap={openMap} onToggle={onToggle} />
              ))}
            </List>
          </Collapse>
        )}
      </Box>
    </Fragment>
  );
}
