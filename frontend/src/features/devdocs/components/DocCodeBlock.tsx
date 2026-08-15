import { useState } from 'react';
import { Box, IconButton, Tooltip, Typography, Chip } from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import CheckIcon from '@mui/icons-material/Check';
import { useAppStore } from '@/shared/stores/useAppStore';

interface Props {
  code: string;
  language?: string;
  filename?: string;
  highlight?: number[];
}

export default function DocCodeBlock({ code, language = 'tsx', filename, highlight = [] }: Props) {
  const [copied, setCopied] = useState(false);
  const { notify } = useAppStore();

  const handleCopy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    notify('Copied to clipboard', 'success');
    setTimeout(() => setCopied(false), 2000);
  };

  const lines = code.split('\n');

  return (
    <Box sx={{ my: 2, borderRadius: 2, overflow: 'hidden', border: '1px solid', borderColor: 'divider' }}>
      {/* Header bar */}
      <Box sx={{ px: 2, py: 0.75, bgcolor: 'action.hover', display: 'flex', alignItems: 'center', gap: 1, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Chip label={language} size="small" variant="outlined" sx={{ fontSize: '0.65rem', height: 18, fontFamily: 'monospace' }} />
        {filename && <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace', flexGrow: 1 }}>{filename}</Typography>}
        <Tooltip title={copied ? 'Copied!' : 'Copy'}>
          <IconButton size="small" onClick={handleCopy} sx={{ color: 'text.secondary' }}>
            {copied ? <CheckIcon fontSize="small" color="success" /> : <ContentCopyIcon fontSize="small" />}
          </IconButton>
        </Tooltip>
      </Box>
      {/* Code body */}
      <Box component="pre" sx={{ m: 0, p: 2, overflowX: 'auto', bgcolor: '#1e1e2e', color: '#cdd6f4', fontSize: '0.82rem', lineHeight: 1.7, fontFamily: '"Fira Code","Cascadia Code","Courier New",monospace' }}>
        {lines.map((line, i) => (
          <Box key={i} component="span" sx={{ display: 'block', px: 0.5, borderRadius: 0.5, bgcolor: highlight.includes(i + 1) ? 'rgba(137,180,250,0.12)' : 'transparent', borderLeft: highlight.includes(i + 1) ? '2px solid #89b4fa' : '2px solid transparent', pl: highlight.includes(i + 1) ? 1 : 0.5 }}>
            <Box component="span" sx={{ userSelect: 'none', color: '#6c7086', mr: 2, fontSize: '0.75rem', display: 'inline-block', minWidth: 24, textAlign: 'right' }}>{i + 1}</Box>
            {line}
          </Box>
        ))}
      </Box>
    </Box>
  );
}
