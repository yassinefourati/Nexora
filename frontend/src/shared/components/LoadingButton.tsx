import { Button, CircularProgress, type ButtonProps } from '@mui/material';
import type { ReactNode } from 'react';

interface LoadingButtonProps extends ButtonProps {
  loading?: boolean;
  loadingLabel?: string;
  children: ReactNode;
}

/**
 * Drop-in replacement for MUI Button that shows a spinner when loading=true.
 * Automatically disables the button during loading to prevent double-submits.
 *
 * Usage:
 *   <LoadingButton loading={isPending} onClick={handleSave}>Save</LoadingButton>
 */
export default function LoadingButton({ loading = false, loadingLabel, children, disabled, startIcon, ...props }: LoadingButtonProps) {
  return (
    <Button
      {...props}
      disabled={disabled ?? loading}
      startIcon={loading ? <CircularProgress size={16} color="inherit" /> : startIcon}
    >
      {loading && loadingLabel ? loadingLabel : children}
    </Button>
  );
}
