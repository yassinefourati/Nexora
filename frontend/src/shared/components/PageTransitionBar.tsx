import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { Box } from '@mui/material';

export default function PageTransitionBar() {
  const { pathname } = useLocation();
  const [progress, setProgress] = useState(0);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    // All setStates happen inside setTimeout callbacks — not synchronously in effect body
    const t0 = setTimeout(() => setVisible(true), 0);
    const t1 = setTimeout(() => setProgress(20), 10);
    const t2 = setTimeout(() => setProgress(60), 80);
    const t3 = setTimeout(() => setProgress(85), 200);
    const t4 = setTimeout(() => setProgress(100), 400);
    const t5 = setTimeout(() => { setVisible(false); setProgress(0); }, 550);
    return () => { [t0,t1,t2,t3,t4,t5].forEach(clearTimeout); setProgress(0); setVisible(false); };
  }, [pathname]);

  if (!visible && progress === 0) return null;

  return (
    <Box sx={{
      position: 'fixed', top: 0, left: 0, zIndex: 2000,
      height: 3, bgcolor: 'primary.main',
      width: `${progress}%`,
      transition: progress === 100 ? 'width 0.1s ease, opacity 0.15s ease' : 'width 0.3s ease',
      opacity: progress === 100 ? 0 : 1,
      boxShadow: '0 0 8px currentColor',
    }} />
  );
}
