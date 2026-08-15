import { Table, TableHead, TableBody, TableRow, TableCell, Chip, Typography, Paper, Box } from '@mui/material';

export interface Prop { name: string; type: string; required?: boolean; default?: string; description: string; }

export default function PropTable({ title, props }: { title?: string; props: Prop[] }) {
  return (
    <Box sx={{ mb: 3 }}>
      {title && <Typography variant="subtitle2" fontWeight={700} mb={1} color="text.secondary">{title}</Typography>}
      <Paper variant="outlined" sx={{ overflow: 'hidden' }}>
        <Table size="small">
          <TableHead>
            <TableRow sx={{ bgcolor: 'action.hover' }}>
              <TableCell sx={{ fontWeight: 700, width: 160, fontFamily: 'monospace', fontSize: '0.78rem' }}>Prop</TableCell>
              <TableCell sx={{ fontWeight: 700, width: 160 }}>Type</TableCell>
              <TableCell sx={{ fontWeight: 700, width: 80 }}>Req</TableCell>
              <TableCell sx={{ fontWeight: 700, width: 110 }}>Default</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>Description</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {props.map((p) => (
              <TableRow key={p.name} hover>
                <TableCell><Typography variant="caption" sx={{ fontFamily: 'monospace', fontWeight: 700, color: 'primary.main' }}>{p.name}</Typography></TableCell>
                <TableCell><Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'text.secondary' }}>{p.type}</Typography></TableCell>
                <TableCell>{p.required ? <Chip label="yes" size="small" color="error" variant="outlined" sx={{ height: 18, fontSize: '0.65rem' }} /> : <Typography variant="caption" color="text.disabled">—</Typography>}</TableCell>
                <TableCell>{p.default ? <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'success.main' }}>{p.default}</Typography> : <Typography variant="caption" color="text.disabled">—</Typography>}</TableCell>
                <TableCell><Typography variant="caption" color="text.secondary">{p.description}</Typography></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Box>
  );
}
