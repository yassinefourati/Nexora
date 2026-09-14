import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Table, TableBody, TableCell, TableHead, TableRow, Chip, CircularProgress, Box } from '@mui/material';
import { useLoanInstallments } from '../hooks/useLoanAccounts';
import type { LoanAccount } from '../api/loanAccountsApi';

interface Props { open: boolean; onClose: () => void; loanAccount: LoanAccount | null; }

const STATUS_COLOR: Record<string, 'default' | 'success' | 'error' | 'warning' | 'info'> = {
  pending: 'default',
  paid: 'success',
  overdue: 'error',
};

export default function InstallmentScheduleDialog({ open, onClose, loanAccount }: Props) {
  const { data: installments, isLoading } = useLoanInstallments(loanAccount?.id);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Repayment Schedule — {loanAccount?.accountNumber}</DialogTitle>
      <DialogContent>
        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}><CircularProgress size={28} /></Box>
        ) : (
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>#</TableCell>
                <TableCell>Due date</TableCell>
                <TableCell align="right">Principal</TableCell>
                <TableCell align="right">Interest</TableCell>
                <TableCell align="right">Total</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(installments ?? []).map((i) => (
                <TableRow key={i.id}>
                  <TableCell>{i.installmentNumber}</TableCell>
                  <TableCell>{i.dueDate}</TableCell>
                  <TableCell align="right">{i.principalAmount}</TableCell>
                  <TableCell align="right">{i.interestAmount}</TableCell>
                  <TableCell align="right">{i.totalAmount}</TableCell>
                  <TableCell><Chip label={i.status} size="small" color={STATUS_COLOR[i.status ?? 'pending']} /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
}
