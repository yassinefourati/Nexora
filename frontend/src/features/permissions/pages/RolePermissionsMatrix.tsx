import { useState } from 'react';
import { Box, Typography, Paper, Table, TableHead, TableBody, TableRow, TableCell, Checkbox, Stack, FormControl, InputLabel, Select, MenuItem, Alert, CircularProgress } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useRoles } from '@/features/roles/hooks/useRoles';
import { usePermissionsList } from '../hooks/usePermissions';
import { useRolePermissions, useGrantRolePermission, useRevokeRolePermission } from '../hooks/usePermissions';
import { usePermission } from '@/shared/hooks/usePermission';

/**
 * Real role -> permission grants, backed by GET/POST/DELETE /role-permissions.
 * Replaces the old fictional /roles/permissions matrix (that endpoint never
 * existed on the backend) with the actual join-table CRUD the API exposes.
 */
export default function RolePermissionsMatrix() {
  const { t } = useTranslation();
  const canEdit = usePermission('role', 'edit');

  const [roleId, setRoleId] = useState<string>('');

  const { data: rolesPage } = useRoles({ page: 0, size: 100 });
  const { data: permissionsPage } = usePermissionsList({ page: 0, size: 200 });
  const { data: grants, isLoading: grantsLoading } = useRolePermissions(roleId || undefined);

  const { mutate: grant, isPending: granting } = useGrantRolePermission();
  const { mutate: revoke, isPending: revoking } = useRevokeRolePermission();

  const roles = rolesPage?.items ?? [];
  const permissions = permissionsPage?.items ?? [];
  const grantedPermissionIds = new Set((grants ?? []).map((g) => g.permissionId));

  const toggle = (permissionId: string) => {
    if (!canEdit || !roleId) return;
    const existing = grants?.find((g) => g.permissionId === permissionId);
    if (existing) {
      revoke({ id: existing.id!, roleId });
    } else {
      grant({ roleId, permissionId });
    }
  };

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2} flexWrap="wrap" gap={2}>
        <Box>
          <Typography variant="h4" component="h1" fontWeight={700}>{t('menu.roles')} · Permissions</Typography>
          <Typography variant="body2" color="text.secondary">
            {canEdit ? 'Select a role, then click checkboxes to grant or revoke permissions.' : 'Read-only — you do not have permission to edit role grants.'}
          </Typography>
        </Box>
        <FormControl size="small" sx={{ minWidth: 220 }}>
          <InputLabel>Role</InputLabel>
          <Select label="Role" value={roleId} onChange={(e) => setRoleId(e.target.value)}>
            {roles.map((r) => <MenuItem key={r.id} value={r.id}>{r.name}</MenuItem>)}
          </Select>
        </FormControl>
      </Stack>

      {!roleId ? (
        <Alert severity="info">Select a role to view and edit its granted permissions.</Alert>
      ) : (
        <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden', position: 'relative' }}>
          {(grantsLoading || granting || revoking) && (
            <Box sx={{ position: 'absolute', inset: 0, bgcolor: 'background.paper', opacity: 0.6, display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1 }}>
              <CircularProgress size={28} aria-label="Loading" />
            </Box>
          )}
          <Table size="small" stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>Code</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Resource</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Action</TableCell>
                <TableCell sx={{ fontWeight: 700 }} align="center">Granted</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {permissions.map((p) => (
                <TableRow key={p.id} hover onClick={() => toggle(p.id!)} sx={{ cursor: canEdit ? 'pointer' : 'default' }}>
                  <TableCell>{p.code}</TableCell>
                  <TableCell>{p.resource}</TableCell>
                  <TableCell>{p.action}</TableCell>
                  <TableCell align="center">
                    <Checkbox checked={grantedPermissionIds.has(p.id!)} disabled={!canEdit} size="small" color="success" />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}
    </Box>
  );
}
