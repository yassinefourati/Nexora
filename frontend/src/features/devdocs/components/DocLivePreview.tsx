import { Box, Paper, Tabs, Tab, Typography } from '@mui/material';
import { useState, type ReactNode } from 'react';
import DocCodeBlock from './DocCodeBlock';

interface Props {
  preview: ReactNode;
  code: string;
  language?: string;
  title?: string;
}

export default function DocLivePreview({ preview, code, language = 'tsx', title }: Props) {
  const [tab, setTab] = useState(0);
  return (
    <Paper variant="outlined" sx={{ mb: 3, overflow: 'hidden' }}>
      {title && <Box sx={{ px: 2, py: 1, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'action.hover' }}><Typography variant="caption" fontWeight={600} color="text.secondary">{title}</Typography></Box>}
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ px: 2, borderBottom: '1px solid', borderColor: 'divider', minHeight: 40 }}>
        <Tab label="Preview" sx={{ minHeight: 40, textTransform: 'none', fontSize: '0.8rem' }} />
        <Tab label="Code" sx={{ minHeight: 40, textTransform: 'none', fontSize: '0.8rem' }} />
      </Tabs>
      {tab === 0 && (
        <Box sx={{ p: 3, bgcolor: 'background.default', minHeight: 80, display: 'flex', alignItems: 'flex-start', flexWrap: 'wrap', gap: 2 }}>
          {preview}
        </Box>
      )}
      {tab === 1 && <DocCodeBlock code={code} language={language} />}
    </Paper>
  );
}
