import { Table, TableHead, TableBody, TableRow, TableCell, Chip, Typography, Paper } from '@mui/material';

export interface PropDef {
  name: string;
  type: string;
  required?: boolean;
  default?: string;
  description: string;
}

export default function DocPropTable({ props }: { props: PropDef[] }) {
  return (
    <Paper variant="outlined" sx={{ overflow: 'hidden', mb: 3 }}>
      <Table size="small">
        <TableHead>
          <TableRow sx={{ bgcolor: 'action.hover' }}>
            <TableCell sx={{ fontWeight: 700, width: 160 }}>Prop</TableCell>
            <TableCell sx={{ fontWeight: 700, width: 180 }}>Type</TableCell>
            <TableCell sx={{ fontWeight: 700, width: 80 }}>Required</TableCell>
            <TableCell sx={{ fontWeight: 700, width: 120 }}>Default</TableCell>
            <TableCell sx={{ fontWeight: 700 }}>Description</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {props.map((p) => (
            <TableRow key={p.name} hover>
              <TableCell><Typography variant="caption" sx={{ fontFamily: 'monospace', fontWeight: 600, color: 'primary.main' }}>{p.name}</Typography></TableCell>
              <TableCell><Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'text.secondary' }}>{p.type}</Typography></TableCell>
              <TableCell>{p.required ? <Chip label="yes" size="small" color="error" variant="outlined" /> : <Typography variant="caption" color="text.disabled">—</Typography>}</TableCell>
              <TableCell>{p.default ? <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'text.secondary' }}>{p.default}</Typography> : <Typography variant="caption" color="text.disabled">—</Typography>}</TableCell>
              <TableCell><Typography variant="caption" color="text.secondary">{p.description}</Typography></TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  );
}
