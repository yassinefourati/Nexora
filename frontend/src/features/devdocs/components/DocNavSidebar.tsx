import { Box, List, ListItemButton, ListItemText, Typography, Divider, Chip } from '@mui/material';

export interface NavItem {
  id: string;
  label: string;
  badge?: string;
  children?: NavItem[];
}

interface Props {
  items: NavItem[];
  activeId: string;
}

export default function DocNavSidebar({ items, activeId }: Props) {
  const scroll = (id: string) => {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  return (
    <Box sx={{ position: 'sticky', top: 80, maxHeight: 'calc(100vh - 100px)', overflowY: 'auto' }}>
      <Typography variant="overline" color="text.secondary" sx={{ px: 2, display: 'block', mb: 1 }}>On this page</Typography>
      <List dense disablePadding>
        {items.map((item) => (
          <Box key={item.id}>
            <ListItemButton onClick={() => scroll(item.id)} selected={activeId === item.id} sx={{ borderRadius: 1, mx: 0.5, py: 0.5 }}>
              <ListItemText primary={<Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}><Typography variant="body2" fontWeight={activeId === item.id ? 700 : 400}>{item.label}</Typography>{item.badge && <Chip label={item.badge} size="small" color="primary" sx={{ height: 16, fontSize: '0.6rem' }} />}</Box>} />
            </ListItemButton>
            {item.children?.map((child) => (
              <ListItemButton key={child.id} onClick={() => scroll(child.id)} sx={{ pl: 3.5, py: 0.25, borderRadius: 1, mx: 0.5 }}>
                <ListItemText primaryTypographyProps={{ variant: 'caption', color: activeId === child.id ? 'primary.main' : 'text.secondary' }} primary={child.label} />
              </ListItemButton>
            ))}
            <Divider sx={{ my: 0.5, mx: 2, opacity: 0.5 }} />
          </Box>
        ))}
      </List>
    </Box>
  );
}
