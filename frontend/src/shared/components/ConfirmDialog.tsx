import { Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button } from '@mui/material';
import { useConfirmStore } from '@/shared/stores/useConfirmStore';
import { useTranslation } from 'react-i18next';
export default function ConfirmDialog() {
  const { open, options, close } = useConfirmStore();
  const { t } = useTranslation();
  return (
    <Dialog open={open} onClose={close} maxWidth="xs" fullWidth>
      <DialogTitle>{options?.title}</DialogTitle>
      <DialogContent><DialogContentText>{options?.message}</DialogContentText></DialogContent>
      <DialogActions>
        <Button onClick={close}>{t('common.cancel')}</Button>
        <Button variant="contained" color={options?.severity === 'error' ? 'error' : 'primary'} onClick={() => { options?.onConfirm(); close(); }} autoFocus>
          {options?.confirmLabel ?? t('common.confirm')}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
