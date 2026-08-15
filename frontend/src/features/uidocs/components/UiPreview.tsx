import { Box, Paper, Typography, Chip } from '@mui/material';
import { useState, type ReactNode } from 'react';
import DocCodeBlock from '@/features/devdocs/components/DocCodeBlock';

interface Variant {
  label: string;
  preview: ReactNode;
  code: string;
  do?: string;
  dont?: string;
}

interface Props {
  title?: string;
  description?: string;
  variants: Variant[];
  fullWidth?: boolean;
}

export default function UiPreview({ title, description, variants, fullWidth = false }: Props) {
  const [selected, setSelected] = useState(0);
  const [showCode, setShowCode] = useState(false);
  const v = variants[selected];

  return (
    <Paper variant="outlined" sx={{ mb: 4, overflow: 'hidden', borderRadius: 2 }}>
      {(title || description) && (
        <Box sx={{ px: 2.5, pt: 2, pb: 1 }}>
          {title && <Typography variant="subtitle2" fontWeight={700}>{title}</Typography>}
          {description && <Typography variant="caption" color="text.secondary">{description}</Typography>}
        </Box>
      )}

      {/* Variant tabs */}
      {variants.length > 1 && (
        <Box sx={{ px: 2, borderBottom: '1px solid', borderColor: 'divider', display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
          {variants.map((vr, i) => (
            <Box key={i} onClick={() => setSelected(i)} sx={{ px: 1.5, py: 1, cursor: 'pointer', borderBottom: '2px solid', borderColor: selected === i ? 'primary.main' : 'transparent', color: selected === i ? 'primary.main' : 'text.secondary', fontSize: '0.8rem', fontWeight: selected === i ? 700 : 400, transition: '0.15s' }}>
              {vr.label}
            </Box>
          ))}
        </Box>
      )}

      {/* Preview area */}
      <Box sx={{ bgcolor: 'background.default', p: 3, minHeight: 80, display: 'flex', flexWrap: 'wrap', gap: 2, alignItems: 'flex-start', ...(fullWidth && { flexDirection: 'column' }) }}>
        {v.preview}
      </Box>

      {/* Do / Don't */}
      {(v.do || v.dont) && (
        <Box sx={{ display: 'flex', gap: 0, borderTop: '1px solid', borderColor: 'divider' }}>
          {v.do && (
            <Box sx={{ flex: 1, p: 2, borderRight: v.dont ? '1px solid' : 'none', borderColor: 'divider', bgcolor: 'success.main', opacity: 0.08 }}>
              <Chip label="✓ Do" size="small" color="success" sx={{ mb: 0.5 }} />
              <Typography variant="caption" display="block" color="text.secondary">{v.do}</Typography>
            </Box>
          )}
          {v.dont && (
            <Box sx={{ flex: 1, p: 2, bgcolor: 'error.main', opacity: 0.08 }}>
              <Chip label="✗ Don't" size="small" color="error" sx={{ mb: 0.5 }} />
              <Typography variant="caption" display="block" color="text.secondary">{v.dont}</Typography>
            </Box>
          )}
        </Box>
      )}

      {/* Code toggle */}
      <Box sx={{ borderTop: '1px solid', borderColor: 'divider' }}>
        <Box onClick={() => setShowCode(p => !p)} sx={{ px: 2, py: 1, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 1, color: 'text.secondary', fontSize: '0.8rem', '&:hover': { bgcolor: 'action.hover' }, userSelect: 'none' }}>
          <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>{showCode ? '▼' : '▶'} View code</Typography>
        </Box>
        {showCode && <DocCodeBlock code={v.code} language="tsx" />}
      </Box>
    </Paper>
  );
}
