import { useState } from 'react';
import { Popover, Button, Stack, FormControlLabel, Checkbox, Typography, Divider, Box } from '@mui/material';
import ViewColumnIcon from '@mui/icons-material/ViewColumn';
import type { ColumnDef } from './types';

interface Props<T> {
  columns: ColumnDef<T>[];
  visible: Set<string>;
  onToggle: (field: string) => void;
}

export default function ColumnManager<T extends object>({ columns, visible, onToggle }: Props<T>) {
  const [anchor, setAnchor] = useState<null | HTMLElement>(null);
  const filterable = columns.filter((c) => c.field !== 'actions');

  return (
    <>
      <Button size="small" variant="outlined" startIcon={<ViewColumnIcon />} onClick={(e) => setAnchor(e.currentTarget)}>
        Columns
      </Button>
      <Popover open={Boolean(anchor)} anchorEl={anchor} onClose={() => setAnchor(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }} transformOrigin={{ vertical: 'top', horizontal: 'right' }}>
        <Box sx={{ p: 2, minWidth: 180 }}>
          <Typography variant="caption" fontWeight={700} color="text.secondary" display="block" mb={1}>SHOW / HIDE COLUMNS</Typography>
          <Divider sx={{ mb: 1 }} />
          <Stack>
            {filterable.map((col) => (
              <FormControlLabel key={String(col.field)}
                control={<Checkbox size="small" checked={visible.has(String(col.field))} onChange={() => onToggle(String(col.field))} />}
                label={<Typography variant="body2">{col.headerName}</Typography>} />
            ))}
          </Stack>
        </Box>
      </Popover>
    </>
  );
}
