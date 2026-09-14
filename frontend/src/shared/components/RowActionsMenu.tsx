import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { IconButton, Menu, MenuItem, ListItemIcon, ListItemText, Tooltip } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import type { ReactNode, MouseEvent } from 'react';
export interface RowAction { label: string; icon: ReactNode; onClick: () => void; color?: 'error' | 'inherit'; hidden?: boolean; }
interface RowActionsMenuProps { actions: RowAction[]; }
export default function RowActionsMenu({ actions }: RowActionsMenuProps) {
  const { t } = useTranslation();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const visible = actions.filter((a) => !a.hidden);
  if (!visible.length) return null;
  const close = () => setAnchorEl(null);
  return (
    <>
      <Tooltip title={t('ui.moreActions')}>
        <IconButton size="small" aria-label={t('ui.moreActions')} onClick={(e: MouseEvent<HTMLElement>) => { e.stopPropagation(); setAnchorEl(e.currentTarget); }}>
          <MoreVertIcon fontSize="small" />
        </IconButton>
      </Tooltip>
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={close} onClick={(e) => e.stopPropagation()}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }} anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}>
        {visible.map((a) => (
          <MenuItem key={a.label} onClick={() => { close(); a.onClick(); }} sx={a.color === 'error' ? { color: 'error.main' } : undefined}>
            <ListItemIcon sx={a.color === 'error' ? { color: 'error.main' } : undefined}>{a.icon}</ListItemIcon>
            <ListItemText>{a.label}</ListItemText>
          </MenuItem>
        ))}
      </Menu>
    </>
  );
}
