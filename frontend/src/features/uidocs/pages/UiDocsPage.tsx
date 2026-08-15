import {
  Box, Grid, Typography, Stack, Button, IconButton, Chip, Alert, AlertTitle,
  TextField, Select, MenuItem, FormControl, InputLabel, FormHelperText,
  Checkbox, FormControlLabel, Switch, Radio, RadioGroup,
  Paper, Card, CardContent, CardActions, CardHeader,
  Avatar, Badge, Tooltip, LinearProgress, CircularProgress,
  Table, TableHead, TableBody, TableRow, TableCell,
  List, ListItem, ListItemText, ListItemIcon, ListItemButton,
  Tabs, Tab, Accordion, AccordionSummary, AccordionDetails,
  Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions,
  Snackbar,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import SearchIcon from '@mui/icons-material/Search';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PeopleIcon from '@mui/icons-material/People';
import { useState } from 'react';
import UiSection from '../components/UiSection';
import UiPreview from '../components/UiPreview';
import PropTable from '../components/PropTable';
import DocCodeBlock from '@/features/devdocs/components/DocCodeBlock';
import LoadingButton from '@/shared/components/LoadingButton';

const NAV = [
  { id: 'buttons',      label: 'Button',       emoji: '🔘' },
  { id: 'loadingbtn',   label: 'LoadingButton', emoji: '⏳' },
  { id: 'inputs',       label: 'TextField',     emoji: '✏️' },
  { id: 'selects',      label: 'Select',        emoji: '📋' },
  { id: 'checkboxes',   label: 'Checkbox & Switch', emoji: '☑️' },
  { id: 'chips',        label: 'Chip',          emoji: '🏷️' },
  { id: 'alerts',       label: 'Alert',         emoji: '⚠️' },
  { id: 'datagrid',     label: 'DataGrid',      emoji: '📊' },
  { id: 'cards',        label: 'Card',          emoji: '🃏' },
  { id: 'dialogs',      label: 'Dialog',        emoji: '💬' },
  { id: 'avatars',      label: 'Avatar & Badge',emoji: '👤' },
  { id: 'tables',       label: 'Table',         emoji: '📋' },
  { id: 'lists',        label: 'List',          emoji: '📝' },
  { id: 'tabs',         label: 'Tabs',          emoji: '🗂️' },
  { id: 'accordion',    label: 'Accordion',     emoji: '🪗' },
  { id: 'progress',     label: 'Progress',      emoji: '⏱️' },
  { id: 'feedback',     label: 'Snackbar',      emoji: '🔔' },
  { id: 'layout',       label: 'Grid & Stack',  emoji: '🏗️' },
  { id: 'typography',   label: 'Typography',    emoji: '✍️' },
];

const DG_ROWS = [
  { id: 1, name: 'Alice Martin', role: 'admin',  status: 'active',   joined: '2024-01-15' },
  { id: 2, name: 'Bob Tremblay', role: 'editor', status: 'active',   joined: '2024-02-20' },
  { id: 3, name: 'Claire Dubois',role: 'viewer', status: 'inactive', joined: '2024-03-10' },
];

const DG_COLS: GridColDef[] = [
  { field: 'name',   headerName: 'Name',   flex: 1 },
  { field: 'role',   headerName: 'Role',   width: 100 },
  { field: 'status', headerName: 'Status', width: 110,
    renderCell: ({ value }) => <Chip label={value} size="small" color={value === 'active' ? 'success' : 'default'} /> },
  { field: 'joined', headerName: 'Joined', width: 120 },
];

export default function UiDocsPage() {
  const [tabVal, setTabVal] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [snackOpen, setSnackOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [checked, setChecked] = useState(false);
  const [radio, setRadio] = useState('a');
  const [activeNav, setActiveNav] = useState('buttons');

  const simulateLoad = () => { setLoading(true); setTimeout(() => setLoading(false), 2000); };

  return (
    <Box>
      {/* Header */}
      <Stack direction="row" spacing={2} alignItems="center" mb={1}>
        <Typography variant="h3" fontWeight={800}>UI Component Library</Typography>
        <Chip label="MUI v7" color="primary" />
        <Chip label="Live examples" color="success" size="small" />
      </Stack>
      <Typography variant="body1" color="text.secondary" mb={4} sx={{ maxWidth: 640 }}>
        Every component used in this project — live previews, copy-ready code, props reference, and do/don't guidelines for the dev team.
      </Typography>

      <Grid container spacing={3}>
        {/* Sticky sidebar */}
        <Grid size={{ xs: 12, md: 2.5 }}>
          <Box sx={{ position: 'sticky', top: 80, maxHeight: 'calc(100vh - 100px)', overflowY: 'auto' }}>
            <Typography variant="overline" color="text.secondary" sx={{ px: 1, display: 'block', mb: 0.5 }}>Components</Typography>
            {NAV.map((item) => (
              <Box key={item.id} onClick={() => { document.getElementById(item.id)?.scrollIntoView({ behavior: 'smooth', block: 'start' }); setActiveNav(item.id); }}
                sx={{ px: 1.5, py: 0.6, cursor: 'pointer', borderRadius: 1, display: 'flex', alignItems: 'center', gap: 1, mb: 0.25,
                  bgcolor: activeNav === item.id ? 'primary.main' : 'transparent',
                  color: activeNav === item.id ? 'primary.contrastText' : 'text.primary',
                  '&:hover': { bgcolor: activeNav === item.id ? 'primary.dark' : 'action.hover' }, transition: '0.15s' }}>
                <Typography variant="body2" sx={{ fontSize: '0.85rem' }}>{item.emoji} {item.label}</Typography>
              </Box>
            ))}
          </Box>
        </Grid>

        {/* Main content */}
        <Grid size={{ xs: 12, md: 9.5 }}>

          {/* ══ BUTTON ══════════════════════════════════════════════════ */}
          <UiSection id="buttons" title="Button"
            subtitle="Primary action trigger. Use variant to convey hierarchy: contained > outlined > text. Never use more than one contained button per section.">
            <UiPreview variants={[
              {
                label: 'Variants',
                preview: <>
                  <Button variant="contained">Contained</Button>
                  <Button variant="outlined">Outlined</Button>
                  <Button variant="text">Text</Button>
                </>,
                code: `<Button variant="contained">Contained</Button>
<Button variant="outlined">Outlined</Button>
<Button variant="text">Text</Button>`,
                do: 'One contained button per action group. Use outlined for secondary actions.',
                dont: "Don't use two contained buttons side by side — it creates confusion about priority.",
              },
              {
                label: 'Colors',
                preview: <>
                  <Button variant="contained" color="primary">Primary</Button>
                  <Button variant="contained" color="success">Success</Button>
                  <Button variant="contained" color="error">Delete</Button>
                  <Button variant="contained" color="warning">Warning</Button>
                  <Button variant="outlined" color="error">Cancel</Button>
                </>,
                code: `<Button variant="contained" color="primary">Primary</Button>
<Button variant="contained" color="success">Success</Button>
<Button variant="contained" color="error">Delete</Button>
<Button variant="outlined" color="error">Cancel</Button>`,
                do: 'Use color="error" for destructive actions like Delete.',
                dont: "Don't use color='warning' for delete — red is the convention users expect.",
              },
              {
                label: 'Sizes & Icons',
                preview: <>
                  <Button variant="contained" size="small" startIcon={<AddIcon />}>Small</Button>
                  <Button variant="contained" size="medium" startIcon={<SaveIcon />}>Medium</Button>
                  <Button variant="contained" size="large" startIcon={<EditIcon />}>Large</Button>
                  <IconButton color="primary"><EditIcon /></IconButton>
                  <IconButton color="error"><DeleteIcon /></IconButton>
                </>,
                code: `<Button variant="contained" size="small" startIcon={<AddIcon />}>Add</Button>
<Button variant="contained" startIcon={<SaveIcon />}>Save</Button>
<Button variant="contained" size="large">Large</Button>
// Icon-only buttons:
<IconButton color="primary"><EditIcon /></IconButton>
<IconButton color="error"><DeleteIcon /></IconButton>`,
                do: 'Use IconButton (no label) in table rows. Pair with Tooltip for accessibility.',
                dont: "Don't use size='large' inside tables or toolbars — it throws off alignment.",
              },
              {
                label: 'States',
                preview: <>
                  <Button variant="contained" disabled>Disabled</Button>
                  <Button variant="outlined" disabled>Disabled</Button>
                  <Button variant="contained" startIcon={<CircularProgress size={16} color="inherit" />} disabled>Saving…</Button>
                  <Tooltip title="Save changes"><Button variant="contained"><SaveIcon /></Button></Tooltip>
                </>,
                code: `// Disabled
<Button variant="contained" disabled>Disabled</Button>

// Loading state (use LoadingButton instead — see next section)
<Button variant="contained" disabled startIcon={<CircularProgress size={16} color="inherit" />}>
  Saving…
</Button>

// Always wrap icon-only buttons in Tooltip
<Tooltip title="Save changes">
  <Button variant="contained"><SaveIcon /></Button>
</Tooltip>`,
              },
            ]} />
            <PropTable props={[
              { name: 'variant',    type: "'contained'|'outlined'|'text'", default: "'text'",    description: 'Visual style. Use contained for primary actions, outlined for secondary.' },
              { name: 'color',      type: "'primary'|'secondary'|'error'|'warning'|'success'|'info'", default: "'primary'", description: 'Color theme.' },
              { name: 'size',       type: "'small'|'medium'|'large'", default: "'medium'",        description: 'Button height. Use small inside tables and toolbars.' },
              { name: 'startIcon',  type: 'ReactNode',                                              description: 'Icon placed before the label text.' },
              { name: 'endIcon',    type: 'ReactNode',                                              description: 'Icon placed after the label text.' },
              { name: 'disabled',   type: 'boolean',                 default: 'false',              description: 'Prevents interaction and dims the button.' },
              { name: 'fullWidth',  type: 'boolean',                 default: 'false',              description: 'Stretches to fill parent width. Use in forms and modals.' },
              { name: 'onClick',    type: '() => void',                                             description: 'Click handler.' },
              { name: 'type',       type: "'button'|'submit'|'reset'",default: "'button'",          description: "Use type='submit' inside <form> elements." },
            ]} />
          </UiSection>

          {/* ══ LOADING BUTTON ══════════════════════════════════════════ */}
          <UiSection id="loadingbtn" title="LoadingButton" badge="Custom" badgeColor="success"
            subtitle="Project-specific wrapper around MUI Button. Shows a spinner, blocks interaction, and prevents double-submits automatically."
            note="Import from @/shared/components/LoadingButton — not from MUI. This replaces the pattern of manually managing disabled + CircularProgress on every form button.">
            <UiPreview variants={[
              {
                label: 'Demo',
                preview: <>
                  <LoadingButton variant="contained" loading={loading} onClick={simulateLoad}>
                    {loading ? 'Saving…' : 'Save Changes'}
                  </LoadingButton>
                  <LoadingButton variant="outlined" loading={loading} loadingLabel="Deleting…" color="error" onClick={simulateLoad}>
                    Delete
                  </LoadingButton>
                  <LoadingButton variant="contained" loading={false} disabled>Already disabled</LoadingButton>
                </>,
                code: `import LoadingButton from '@/shared/components/LoadingButton';

const { mutate, isPending } = useMutation({ mutationFn: saveUser });

// Spinner appears, button disables automatically when loading=true
<LoadingButton
  variant="contained"
  loading={isPending}
  onClick={() => mutate(formData)}
>
  Save Changes
</LoadingButton>

// With custom loading label
<LoadingButton
  variant="outlined"
  color="error"
  loading={isPending}
  loadingLabel="Deleting…"
  onClick={() => mutate(id)}
>
  Delete
</LoadingButton>`,
                do: 'Always use LoadingButton on form submits and mutation triggers — never raw Button + manual isPending.',
                dont: "Don't add your own CircularProgress startIcon — LoadingButton handles that.",
              },
            ]} />
            <PropTable props={[
              { name: 'loading',      type: 'boolean', default: 'false',   description: 'When true: shows spinner, disables button, prevents clicks.' },
              { name: 'loadingLabel', type: 'string',                       description: 'Optional label shown while loading. If omitted, original children are shown.' },
              { name: '...ButtonProps', type: 'ButtonProps',                description: 'All standard MUI Button props are supported (variant, color, size, etc.).' },
            ]} />
          </UiSection>

          {/* ══ TEXT FIELD ══════════════════════════════════════════════ */}
          <UiSection id="inputs" title="TextField"
            subtitle="Standard input. Always use with react-hook-form + Zod in this project. Never manage input value manually with useState unless the field is uncontrolled by design.">
            <UiPreview variants={[
              {
                label: 'Variants',
                preview: (
                  <Stack spacing={2} sx={{ width: '100%', maxWidth: 400 }}>
                    <TextField label="Outlined (default)" variant="outlined" />
                    <TextField label="Filled" variant="filled" />
                    <TextField label="Standard" variant="standard" />
                  </Stack>
                ),
                code: `// Always use outlined (default) in this project for consistency
<TextField label="Name" variant="outlined" />`,
                do: "Use variant='outlined' everywhere for visual consistency. Other variants are available but not used in this project.",
                dont: "Don't mix variants in the same form.",
              },
              {
                label: 'States',
                preview: (
                  <Stack spacing={2} sx={{ width: '100%', maxWidth: 400 }}>
                    <TextField label="Normal" />
                    <TextField label="With helper text" helperText="This will appear on your profile." />
                    <TextField label="Error state" error helperText="Email is required." />
                    <TextField label="Disabled" disabled value="Cannot edit" />
                    <TextField label="Read-only" InputProps={{ readOnly: true }} value="Read only value" />
                  </Stack>
                ),
                code: `<TextField label="Normal" />
<TextField label="With helper" helperText="Appears below input." />
<TextField label="Error" error helperText="Error message here." />
<TextField label="Disabled" disabled />
<TextField label="Read-only" InputProps={{ readOnly: true }} />`,
                do: 'Use error + helperText together — error colors the label+border, helperText shows the message.',
                dont: "Don't use error={true} without a helperText — the user can't see what's wrong.",
              },
              {
                label: 'With react-hook-form',
                preview: (
                  <Alert severity="info">This pattern is used on every form in the project.</Alert>
                ),
                code: `import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const schema = z.object({
  name:  z.string().min(2, 'Min 2 characters'),
  email: z.string().email('Invalid email'),
});

function MyForm() {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
  });

  return (
    <form onSubmit={handleSubmit(console.log)}>
      <TextField
        label="Name"
        {...register('name')}          // ← spread register — handles value, onChange, ref
        error={!!errors.name}          // ← true when validation fails
        helperText={errors.name?.message} // ← shows Zod error message
      />
      <TextField label="Email" {...register('email')} error={!!errors.email} helperText={errors.email?.message} />
      <Button type="submit">Submit</Button>
    </form>
  );
}`,
              },
              {
                label: 'Sizes & Adornments',
                preview: (
                  <Stack spacing={2} sx={{ width: '100%', maxWidth: 400 }}>
                    <TextField label="Small" size="small" />
                    <TextField label="Search" size="small" InputProps={{ startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} /> }} />
                    <TextField label="Price" InputProps={{ startAdornment: <Typography sx={{ mr: 0.5, color: 'text.secondary' }}>$</Typography> }} />
                    <TextField label="Multiline" multiline rows={3} />
                  </Stack>
                ),
                code: `// Size small — use inside toolbars and filter bars
<TextField size="small" label="Search" />

// With adornment icons
<TextField
  label="Search"
  size="small"
  InputProps={{ startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} /> }}
/>

// Price input with prefix
<TextField
  label="Price"
  InputProps={{ startAdornment: <Typography sx={{ mr: 0.5 }}>$</Typography> }}
/>

// Multiline (textarea)
<TextField label="Notes" multiline rows={3} />`,
              },
            ]} />
            <PropTable props={[
              { name: 'label',        type: 'string',                                         description: 'Floating label text.' },
              { name: 'value',        type: 'string',                                         description: 'Controlled value. Use register() from RHF instead.' },
              { name: 'error',        type: 'boolean',        default: 'false',               description: 'Sets error state (red border + label).' },
              { name: 'helperText',   type: 'ReactNode',                                      description: 'Text shown below the field. Show validation message here.' },
              { name: 'disabled',     type: 'boolean',        default: 'false',               description: 'Prevents interaction.' },
              { name: 'size',         type: "'small'|'medium'", default: "'medium'",          description: "Use 'small' in dense UIs like filter bars." },
              { name: 'multiline',    type: 'boolean',        default: 'false',               description: 'Turns into a textarea.' },
              { name: 'rows',         type: 'number',                                         description: 'Fixed number of visible rows. Requires multiline=true.' },
              { name: 'type',         type: 'string',         default: "'text'",              description: "HTML input type: 'text', 'password', 'email', 'number'." },
              { name: 'InputProps',   type: 'object',                                         description: 'Props for the inner Input. Use for startAdornment/endAdornment/readOnly.' },
              { name: 'fullWidth',    type: 'boolean',        default: 'false',               description: 'Fills parent width. Always use in forms.' },
            ]} />
          </UiSection>

          {/* ══ SELECT ══════════════════════════════════════════════════ */}
          <UiSection id="selects" title="Select"
            subtitle="Dropdown picker. For static options (roles, statuses, categories). For async-loaded options, use Autocomplete from MUI instead.">
            <UiPreview variants={[
              {
                label: 'Basic',
                preview: (
                  <FormControl sx={{ minWidth: 200 }}>
                    <InputLabel>Role</InputLabel>
                    <Select label="Role" defaultValue="viewer">
                      <MenuItem value="admin">Admin</MenuItem>
                      <MenuItem value="editor">Editor</MenuItem>
                      <MenuItem value="viewer">Viewer</MenuItem>
                    </Select>
                  </FormControl>
                ),
                code: `import { Select, MenuItem, FormControl, InputLabel } from '@mui/material';

// Always wrap in FormControl + InputLabel — otherwise the label floats wrong
<FormControl fullWidth>
  <InputLabel>Role</InputLabel>
  <Select label="Role" defaultValue="viewer">
    <MenuItem value="admin">Admin</MenuItem>
    <MenuItem value="editor">Editor</MenuItem>
    <MenuItem value="viewer">Viewer</MenuItem>
  </Select>
</FormControl>`,
                do: "Always pass label to both InputLabel and Select — the 'label' prop on Select cuts the outline correctly.",
                dont: "Don't use Select without FormControl — the floating label won't work.",
              },
              {
                label: 'With RHF + error',
                preview: (
                  <Stack spacing={2} sx={{ maxWidth: 300 }}>
                    <FormControl error>
                      <InputLabel>Role *</InputLabel>
                      <Select label="Role *" value=""><MenuItem value="">None</MenuItem></Select>
                      <FormHelperText>Role is required</FormHelperText>
                    </FormControl>
                    <FormControl size="small">
                      <InputLabel>Size small</InputLabel>
                      <Select label="Size small" defaultValue="a">
                        <MenuItem value="a">Option A</MenuItem>
                        <MenuItem value="b">Option B</MenuItem>
                      </Select>
                    </FormControl>
                  </Stack>
                ),
                code: `// In react-hook-form, use Controller (not register) for Select:
import { Controller } from 'react-hook-form';

<Controller
  name="role"
  control={control}
  render={({ field, fieldState }) => (
    <FormControl fullWidth error={!!fieldState.error}>
      <InputLabel>Role</InputLabel>
      <Select {...field} label="Role">
        <MenuItem value="admin">Admin</MenuItem>
        <MenuItem value="editor">Editor</MenuItem>
        <MenuItem value="viewer">Viewer</MenuItem>
      </Select>
      <FormHelperText>{fieldState.error?.message}</FormHelperText>
    </FormControl>
  )}
/>`,
              },
            ]} />
          </UiSection>

          {/* ══ CHECKBOX & SWITCH ════════════════════════════════════════ */}
          <UiSection id="checkboxes" title="Checkbox, Switch & Radio"
            subtitle="Binary toggles. Use Checkbox for multi-select lists, Switch for on/off settings, Radio for single-select from a small set.">
            <UiPreview variants={[
              {
                label: 'Checkbox',
                preview: <>
                  <FormControlLabel control={<Checkbox defaultChecked />} label="Enabled" />
                  <FormControlLabel control={<Checkbox />} label="Disabled option" />
                  <FormControlLabel control={<Checkbox color="success" defaultChecked />} label="Success color" />
                  <FormControlLabel control={<Checkbox />} label="Unchecked" />
                </>,
                code: `<FormControlLabel
  control={<Checkbox defaultChecked />}
  label="Enable notifications"
/>

// Controlled with useState:
const [checked, setChecked] = useState(false);
<Checkbox
  checked={checked}
  onChange={(e) => setChecked(e.target.checked)}
/>

// With react-hook-form:
<FormControlLabel
  control={<Checkbox {...register('agreeToTerms')} />}
  label="I agree to the terms"
/>`,
              },
              {
                label: 'Switch',
                preview: <>
                  <FormControlLabel control={<Switch checked={checked} onChange={e => setChecked(e.target.checked)} />} label={checked ? 'On' : 'Off'} />
                  <FormControlLabel control={<Switch defaultChecked color="success" />} label="Email notifications" />
                  <FormControlLabel control={<Switch />} label="Push notifications" />
                </>,
                code: `// For settings toggles (like in Settings › Notifications):
const [enabled, setEnabled] = useState(true);
<FormControlLabel
  control={
    <Switch
      checked={enabled}
      onChange={(e) => setEnabled(e.target.checked)}
    />
  }
  label="Email notifications"
/>`,
                do: 'Use Switch for settings that take immediate effect. Use Checkbox for form fields submitted later.',
                dont: "Don't use Switch in forms that require a Save button — users expect switch changes to be immediate.",
              },
              {
                label: 'Radio',
                preview: (
                  <RadioGroup value={radio} onChange={e => setRadio(e.target.value)}>
                    <FormControlLabel value="a" control={<Radio />} label="Option A" />
                    <FormControlLabel value="b" control={<Radio />} label="Option B" />
                    <FormControlLabel value="c" control={<Radio />} label="Option C (disabled)" disabled />
                  </RadioGroup>
                ),
                code: `const [value, setValue] = useState('a');

<RadioGroup value={value} onChange={(e) => setValue(e.target.value)}>
  <FormControlLabel value="a" control={<Radio />} label="Option A" />
  <FormControlLabel value="b" control={<Radio />} label="Option B" />
</RadioGroup>`,
                do: 'Use Radio when options are mutually exclusive and there are 2–6 choices.',
                dont: "Don't use Radio for more than 6 options — use Select instead.",
              },
            ]} />
          </UiSection>

          {/* ══ CHIP ════════════════════════════════════════════════════ */}
          <UiSection id="chips" title="Chip"
            subtitle="Compact label for status, role, category, or tag. Use inside table cells, next to headings, or in filter bars. Never use for primary actions.">
            <UiPreview variants={[
              {
                label: 'Colors & variants',
                preview: <>
                  <Chip label="default" />
                  <Chip label="primary" color="primary" />
                  <Chip label="success" color="success" />
                  <Chip label="warning" color="warning" />
                  <Chip label="error" color="error" />
                  <Chip label="info" color="info" />
                  <Chip label="outlined" variant="outlined" color="primary" />
                  <Chip label="small" size="small" color="success" />
                </>,
                code: `// Status chips (most common use in this project):
<Chip label="active"   size="small" color="success" />
<Chip label="inactive" size="small" color="default" />
<Chip label="admin"    size="small" color="error"   variant="outlined" />
<Chip label="editor"   size="small" color="warning" variant="outlined" />
<Chip label="viewer"   size="small" color="default" variant="outlined" />`,
                do: 'Always use size="small" inside table cells. Use filled for status, outlined for role/category.',
                dont: "Don't use Chip for clickable actions — use Button instead.",
              },
              {
                label: 'Interactive',
                preview: <>
                  <Chip label="Clickable" onClick={() => {}} color="primary" variant="outlined" />
                  <Chip label="Deletable" onDelete={() => {}} color="primary" />
                  <Chip avatar={<Avatar sx={{ bgcolor: 'error.main' }}>A</Avatar>} label="Admin user" />
                  <Chip icon={<CheckCircleIcon />} label="Verified" color="success" />
                </>,
                code: `// Clickable filter chip:
<Chip label="Active" onClick={() => toggleFilter('active')} variant="outlined" color="primary" />

// Deletable tag:
<Chip label="React" onDelete={() => removeTag('react')} />

// With avatar:
<Chip avatar={<Avatar>A</Avatar>} label="Alice" />

// With icon:
<Chip icon={<CheckCircleIcon />} label="Verified" color="success" />`,
              },
            ]} />
          </UiSection>

          {/* ══ ALERT ════════════════════════════════════════════════════ */}
          <UiSection id="alerts" title="Alert"
            subtitle="Contextual message for info, success, warning, and error states. Use for form validation summaries, page-level notices, and empty state explanations.">
            <UiPreview fullWidth variants={[
              {
                label: 'Severity',
                preview: (
                  <Stack spacing={1.5} sx={{ width: '100%' }}>
                    <Alert severity="info">This is an informational message.</Alert>
                    <Alert severity="success">Your changes were saved successfully.</Alert>
                    <Alert severity="warning">This action cannot be undone.</Alert>
                    <Alert severity="error">Failed to save. Please try again.</Alert>
                  </Stack>
                ),
                code: `<Alert severity="info">Informational notice.</Alert>
<Alert severity="success">Saved successfully.</Alert>
<Alert severity="warning">This cannot be undone.</Alert>
<Alert severity="error">Something went wrong.</Alert>`,
              },
              {
                label: 'With title & action',
                preview: (
                  <Stack spacing={1.5} sx={{ width: '100%' }}>
                    <Alert severity="warning" onClose={() => {}}>
                      <AlertTitle>Warning</AlertTitle>
                      You have unsaved permission changes.
                    </Alert>
                    <Alert severity="error" variant="filled" action={<Button color="inherit" size="small">Retry</Button>}>
                      Connection failed — the database is unreachable.
                    </Alert>
                  </Stack>
                ),
                code: `import { Alert, AlertTitle } from '@mui/material';

// With title:
<Alert severity="warning" onClose={() => setDismissed(true)}>
  <AlertTitle>Warning</AlertTitle>
  You have unsaved permission changes.
</Alert>

// Filled variant with action button:
<Alert severity="error" variant="filled" action={<Button color="inherit" size="small">Retry</Button>}>
  Connection failed.
</Alert>`,
                do: 'Use filled variant for critical errors that demand immediate attention.',
                dont: "Don't use Alert for transient feedback (saved, deleted) — use Snackbar instead.",
              },
            ]} />
          </UiSection>

          {/* ══ DATAGRID ════════════════════════════════════════════════ */}
          <UiSection id="datagrid" title="DataGrid" badge="MUI X"
            subtitle="Feature-rich table for large, server-paginated datasets. Use for Users, Audit Log, Reports. For small static tables, use the basic Table component instead."
            note="DataGrid requires @mui/x-data-grid. It's already installed. Import from '@mui/x-data-grid'.">
            <UiPreview fullWidth variants={[
              {
                label: 'Server pagination',
                preview: (
                  <Box sx={{ height: 300, width: '100%' }}>
                    <DataGrid
                      rows={DG_ROWS}
                      columns={DG_COLS}
                      rowCount={DG_ROWS.length}
                      paginationMode="server"
                      paginationModel={{ page: 0, pageSize: 10 }}
                      onPaginationModelChange={() => {}}
                      pageSizeOptions={[10, 25, 50]}
                      disableRowSelectionOnClick
                    />
                  </Box>
                ),
                code: `import { DataGrid, type GridColDef } from '@mui/x-data-grid';

// ⚠️ Always wrap in a Box with fixed height — never use autoHeight with server pagination
// autoHeight crashes in MUI X v8 when combined with paginationMode="server"

const columns: GridColDef[] = [
  { field: 'name',  headerName: 'Name',  flex: 1 },
  { field: 'email', headerName: 'Email', flex: 1.5 },
  {
    field: 'role',
    headerName: 'Role',
    width: 120,
    renderCell: ({ value }) => (
      <Chip label={value} size="small" color={value === 'admin' ? 'error' : 'default'} />
    ),
  },
  {
    field: 'actions',
    headerName: '',
    width: 90,
    sortable: false,
    renderCell: ({ row }) => (
      <IconButton size="small" onClick={(e) => { e.stopPropagation(); handleEdit(row); }}>
        <EditIcon fontSize="small" />
      </IconButton>
    ),
  },
];

// rows and rowCount MUST be guaranteed non-undefined values
const rows     = data?.data ?? [];      // ← always []
const rowCount = data?.total ?? 0;      // ← always 0

<Box sx={{ minHeight: 400 }}>
  <DataGrid
    rows={rows}
    columns={columns}
    rowCount={rowCount}
    paginationMode="server"
    paginationModel={{ page, pageSize: 10 }}
    onPaginationModelChange={(m) => setPage(m.page)}
    pageSizeOptions={[10, 25, 50]}
    checkboxSelection
    disableRowSelectionOnClick={false}
    onRowClick={(params) => navigate(\`/users/\${params.id}\`)}
    onRowSelectionModelChange={(m) => setSelected(m)}
    rowSelectionModel={selected}
    sx={{ '& .MuiDataGrid-row': { cursor: 'pointer' } }}
  />
</Box>`,
                do: "Always guarantee rows=[] and rowCount=0 as fallbacks. Always use a fixed-height Box wrapper, never autoHeight with server pagination.",
                dont: "Don't call e.stopPropagation() on the row — do call it on action buttons inside renderCell to prevent row-click navigation from firing.",
              },
            ]} />
            <PropTable title="Key DataGrid props" props={[
              { name: 'rows',                    type: 'T[]',     required: true, description: 'Array of row objects. Each must have a unique id field.' },
              { name: 'columns',                 type: 'GridColDef[]', required: true, description: 'Column definitions. See GridColDef below.' },
              { name: 'rowCount',                type: 'number',  required: true, description: 'Total row count for server pagination. Always provide a fallback ?? 0.' },
              { name: 'paginationMode',          type: "'server'|'client'", default: "'client'", description: "Use 'server' when loading from an API. Use 'client' only for small static datasets." },
              { name: 'paginationModel',         type: '{ page, pageSize }', description: 'Current page (0-indexed) and page size.' },
              { name: 'onPaginationModelChange', type: '(model) => void', description: 'Called when page or pageSize changes.' },
              { name: 'pageSizeOptions',         type: 'number[]', default: '[10,25,50]', description: 'Must include the current pageSize value or MUI will warn.' },
              { name: 'checkboxSelection',       type: 'boolean', default: 'false', description: 'Adds a checkbox column for multi-select.' },
              { name: 'onRowClick',              type: '(params) => void', description: 'Fires when user clicks a row. Call e.stopPropagation() on action buttons inside cells.' },
              { name: 'autoHeight',              type: 'boolean', description: '⚠️ Do NOT use with paginationMode="server" — crashes in MUI X v8.' },
            ]} />
            <PropTable title="GridColDef key props" props={[
              { name: 'field',       type: 'string',           required: true, description: 'Key of the row object.' },
              { name: 'headerName',  type: 'string',           required: true, description: 'Column header label.' },
              { name: 'flex',        type: 'number',                           description: 'Flex-grow ratio. Use instead of width for responsive columns.' },
              { name: 'width',       type: 'number',                           description: 'Fixed pixel width. Use for short columns like status, actions.' },
              { name: 'sortable',    type: 'boolean',          default: 'true', description: 'Set false on action columns.' },
              { name: 'renderCell',  type: '(params) => ReactNode',            description: 'Custom cell renderer. params.row = full row object, params.value = cell value.' },
            ]} />
          </UiSection>

          {/* ══ CARD ════════════════════════════════════════════════════ */}
          <UiSection id="cards" title="Card"
            subtitle="Container for related content. Use Paper (elevation only) for dashboard widgets, Card (with CardHeader/CardContent/CardActions) for content with a title and footer actions.">
            <UiPreview variants={[
              {
                label: 'Paper vs Card',
                preview: <>
                  <Paper elevation={2} sx={{ p: 3, borderRadius: 3, width: 200 }}>
                    <Typography variant="subtitle2" mb={1}>Paper</Typography>
                    <Typography variant="body2" color="text.secondary">Use for stat cards, chart containers, form panels.</Typography>
                  </Paper>
                  <Card sx={{ width: 220, borderRadius: 3 }}>
                    <CardHeader title="User Card" subheader="editor@demo.com" avatar={<Avatar sx={{ bgcolor: 'primary.main' }}>E</Avatar>} />
                    <CardContent><Typography variant="body2" color="text.secondary">Last login: today</Typography></CardContent>
                    <CardActions><Button size="small">View</Button><Button size="small" color="error">Remove</Button></CardActions>
                  </Card>
                </>,
                code: `// Paper — simpler, just elevation + padding
import { Paper } from '@mui/material';
<Paper elevation={2} sx={{ p: 3, borderRadius: 3 }}>
  Content here
</Paper>

// Card — has structured header / content / actions zones
import { Card, CardHeader, CardContent, CardActions } from '@mui/material';
<Card sx={{ borderRadius: 3 }}>
  <CardHeader
    title="User Card"
    subheader="editor@demo.com"
    avatar={<Avatar sx={{ bgcolor: 'primary.main' }}>E</Avatar>}
  />
  <CardContent>
    <Typography variant="body2">Last login: today</Typography>
  </CardContent>
  <CardActions>
    <Button size="small">View</Button>
    <Button size="small" color="error">Remove</Button>
  </CardActions>
</Card>`,
                do: 'Use borderRadius: 3 (= 12px) on all cards for consistency with the rest of the project.',
                dont: "Don't mix Card and Paper in the same view — pick one pattern per section.",
              },
            ]} />
          </UiSection>

          {/* ══ DIALOG ══════════════════════════════════════════════════ */}
          <UiSection id="dialogs" title="Dialog"
            subtitle="Modal popup for confirmations, forms, and detail views. For simple yes/no confirmations, always use useConfirmStore instead of building your own Dialog.">
            <UiPreview variants={[
              {
                label: 'Live demo',
                preview: <>
                  <Button variant="contained" onClick={() => setDialogOpen(true)}>Open Dialog</Button>
                  <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
                    <DialogTitle>Edit User</DialogTitle>
                    <DialogContent>
                      <DialogContentText mb={2}>Update the user's information below.</DialogContentText>
                      <Stack spacing={2}>
                        <TextField label="Name" fullWidth defaultValue="Alice Martin" />
                        <TextField label="Email" fullWidth defaultValue="alice@demo.com" />
                      </Stack>
                    </DialogContent>
                    <DialogActions>
                      <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
                      <LoadingButton variant="contained" loading={false} onClick={() => setDialogOpen(false)}>Save</LoadingButton>
                    </DialogActions>
                  </Dialog>
                </>,
                code: `import { Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions } from '@mui/material';

const [open, setOpen] = useState(false);

<Button onClick={() => setOpen(true)}>Open</Button>

<Dialog
  open={open}
  onClose={() => setOpen(false)}
  maxWidth="sm"     // 'xs'|'sm'|'md'|'lg'|'xl'
  fullWidth         // always use this — makes it fill maxWidth
>
  <DialogTitle>Edit User</DialogTitle>
  <DialogContent>
    <DialogContentText mb={2}>Explanatory text if needed.</DialogContentText>
    <TextField label="Name" fullWidth autoFocus />
  </DialogContent>
  <DialogActions>
    <Button onClick={() => setOpen(false)}>Cancel</Button>
    <LoadingButton variant="contained" loading={isPending} onClick={handleSave}>
      Save
    </LoadingButton>
  </DialogActions>
</Dialog>`,
                do: 'Always put Cancel first, primary action last. Use LoadingButton for the primary action.',
                dont: "Don't build your own confirm dialog — use useConfirmStore. Don't use Dialog without maxWidth + fullWidth.",
              },
            ]} />
            <DocCodeBlock language="tsx" code={`// ✅ Use this for ALL confirmations (delete, irreversible actions):
import { useConfirmStore } from '@/shared/stores/useConfirmStore';

const { confirm } = useConfirmStore();

confirm({
  title: 'Delete user',
  message: 'Remove Alice Martin? This cannot be undone.',
  severity: 'error',       // 'error' | 'warning' | 'info'
  confirmLabel: 'Delete',
  onConfirm: () => deleteUser(id),
});
// The Dialog is already mounted globally — zero JSX to add`} />
          </UiSection>

          {/* ══ AVATAR & BADGE ══════════════════════════════════════════ */}
          <UiSection id="avatars" title="Avatar & Badge"
            subtitle="Avatar displays user identity. Badge overlays a count or dot on any element.">
            <UiPreview variants={[
              {
                label: 'Avatar',
                preview: <>
                  <Avatar>A</Avatar>
                  <Avatar sx={{ bgcolor: 'primary.main' }}>AL</Avatar>
                  <Avatar sx={{ bgcolor: 'error.main' }}>AD</Avatar>
                  <Avatar src="https://i.pravatar.cc/40?u=1" />
                  <Stack direction="row">{['A','B','C'].map((l) => <Avatar key={l} sx={{ width:28,height:28,fontSize:'0.8rem',ml:l==='A'?0:-1, border:'2px solid', borderColor:'background.paper' }}>{l}</Avatar>)}</Stack>
                </>,
                code: `// Letter avatar (initials)
<Avatar sx={{ bgcolor: 'primary.main' }}>AL</Avatar>

// From user name — always use first letter:
<Avatar sx={{ bgcolor: 'primary.main' }}>
  {user.name[0].toUpperCase()}
</Avatar>

// Image avatar (with fallback to letter):
<Avatar src={user.avatarUrl}>
  {user.name[0]}
</Avatar>

// Stacked avatars (team view):
{users.map((u, i) => (
  <Avatar key={u.id} sx={{ width: 28, height: 28, ml: i === 0 ? 0 : -1, border: '2px solid white' }}>
    {u.name[0]}
  </Avatar>
))}`,
              },
              {
                label: 'Badge',
                preview: <>
                  <Badge badgeContent={4} color="error"><Avatar sx={{ bgcolor: 'primary.main' }}>N</Avatar></Badge>
                  <Badge badgeContent={99} max={9} color="error"><Avatar><PeopleIcon /></Avatar></Badge>
                  <Badge variant="dot" color="success"><Avatar sx={{ bgcolor: 'success.main' }}>U</Avatar></Badge>
                  <Badge badgeContent={0} showZero color="primary"><Avatar>Z</Avatar></Badge>
                </>,
                code: `// Notification count (used in NotificationsBell):
<Badge badgeContent={unreadCount} color="error" max={9}>
  <NotificationsIcon />
</Badge>

// Online status dot:
<Badge variant="dot" color="success">
  <Avatar>{user.name[0]}</Avatar>
</Badge>

// Always show 0:
<Badge badgeContent={0} showZero color="primary">
  <MailIcon />
</Badge>`,
              },
            ]} />
          </UiSection>

          {/* ══ TABLE ════════════════════════════════════════════════════ */}
          <UiSection id="tables" title="Table"
            subtitle="Simple HTML-style table. Use for small, static datasets (< 100 rows) where you don't need sorting, filtering, or pagination. For large or paginated data, use DataGrid.">
            <UiPreview fullWidth variants={[
              {
                label: 'Basic table',
                preview: (
                  <Paper variant="outlined" sx={{ overflow: 'hidden', width: '100%' }}>
                    <Table size="small">
                      <TableHead>
                        <TableRow sx={{ bgcolor: 'action.hover' }}>
                          <TableCell sx={{ fontWeight: 700 }}>Service</TableCell>
                          <TableCell sx={{ fontWeight: 700 }}>Status</TableCell>
                          <TableCell sx={{ fontWeight: 700 }}>Latency</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {[['API','healthy',12],['Database','healthy',18],['Cache','degraded',210]].map(([s,st,l]) => (
                          <TableRow key={s} hover>
                            <TableCell>{s}</TableCell>
                            <TableCell><Chip label={st} size="small" color={st==='healthy'?'success':'warning'} /></TableCell>
                            <TableCell sx={{ fontFamily: 'monospace' }}>{l}ms</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </Paper>
                ),
                code: `import { Table, TableHead, TableBody, TableRow, TableCell } from '@mui/material';

<Paper variant="outlined" sx={{ overflow: 'hidden' }}>
  <Table size="small">       {/* size="small" reduces row height */}
    <TableHead>
      <TableRow sx={{ bgcolor: 'action.hover' }}>
        <TableCell sx={{ fontWeight: 700 }}>Service</TableCell>
        <TableCell sx={{ fontWeight: 700 }}>Status</TableCell>
        <TableCell sx={{ fontWeight: 700 }}>Latency</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      {rows.map((row) => (
        <TableRow key={row.id} hover>    {/* hover adds background on mouse-over */}
          <TableCell>{row.service}</TableCell>
          <TableCell>
            <Chip label={row.status} size="small" color={row.status === 'healthy' ? 'success' : 'warning'} />
          </TableCell>
          <TableCell sx={{ fontFamily: 'monospace' }}>{row.latency}ms</TableCell>
        </TableRow>
      ))}
    </TableBody>
  </Table>
</Paper>`,
                do: 'Always wrap Table in Paper with overflow:hidden and add hover to TableRow.',
                dont: 'Don\'t use Table for more than ~100 rows or when users need to sort/filter — use DataGrid.',
              },
            ]} />
          </UiSection>

          {/* ══ LIST ════════════════════════════════════════════════════ */}
          <UiSection id="lists" title="List"
            subtitle="Vertical sequence of items. Use for activity feeds, notifications, session lists, and nav menus.">
            <UiPreview variants={[
              {
                label: 'Variants',
                preview: (
                  <Stack spacing={2} sx={{ width: '100%', maxWidth: 400 }}>
                    <Paper variant="outlined">
                      <List dense disablePadding>
                        {['alice@demo.com created user','bob@demo.com updated role','vera@demo.com viewed report'].map((item, i) => (
                          <ListItem key={i} divider={i < 2}>
                            <ListItemIcon><Avatar sx={{ width:28,height:28,bgcolor:'primary.main',fontSize:'0.75rem' }}>{item[0].toUpperCase()}</Avatar></ListItemIcon>
                            <ListItemText primary={item} secondary="2 min ago" />
                          </ListItem>
                        ))}
                      </List>
                    </Paper>
                    <Paper variant="outlined">
                      <List disablePadding>
                        {['Dashboard','Users','Analytics'].map((item) => (
                          <ListItemButton key={item}><ListItemText primary={item} /></ListItemButton>
                        ))}
                      </List>
                    </Paper>
                  </Stack>
                ),
                code: `// Activity feed (non-clickable):
<List dense disablePadding>
  {activities.map((a, i) => (
    <ListItem key={a.id} divider={i < activities.length - 1}>
      <ListItemIcon>
        <Avatar sx={{ width: 28, height: 28 }}>{a.user[0]}</Avatar>
      </ListItemIcon>
      <ListItemText primary={a.action} secondary={a.timestamp} />
    </ListItem>
  ))}
</List>

// Clickable nav list:
<List disablePadding>
  {items.map((item) => (
    <ListItemButton key={item.path} selected={pathname === item.path} onClick={() => navigate(item.path)}>
      <ListItemIcon>{item.icon}</ListItemIcon>
      <ListItemText primary={item.label} />
    </ListItemButton>
  ))}
</List>`,
              },
            ]} />
          </UiSection>

          {/* ══ TABS ════════════════════════════════════════════════════ */}
          <UiSection id="tabs" title="Tabs"
            subtitle="Horizontal navigation between related sections within a page. Use for Profile (General / Password / 2FA / Sessions) and detail views.">
            <UiPreview fullWidth variants={[
              {
                label: 'Basic tabs',
                preview: (
                  <Box sx={{ width: '100%' }}>
                    <Tabs value={tabVal} onChange={(_, v) => setTabVal(v)} sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
                      <Tab label="General" />
                      <Tab label="Security" />
                      <Tab label="Sessions" />
                    </Tabs>
                    <Box>
                      {tabVal === 0 && <Typography variant="body2" color="text.secondary">General settings content</Typography>}
                      {tabVal === 1 && <Typography variant="body2" color="text.secondary">Password and 2FA settings</Typography>}
                      {tabVal === 2 && <Typography variant="body2" color="text.secondary">Active sessions list</Typography>}
                    </Box>
                  </Box>
                ),
                code: `const [tab, setTab] = useState(0);

// Tabs always go above a divider
<Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
  <Tab label="General" />
  <Tab label="Security" />
  <Tab label="Sessions" />
</Tabs>

{/* Render the right panel: */}
{tab === 0 && <GeneralPanel />}
{tab === 1 && <SecurityPanel />}
{tab === 2 && <SessionsPanel />}

// For many tabs, add scrollable:
<Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" scrollButtons="auto">`,
              },
            ]} />
          </UiSection>

          {/* ══ ACCORDION ════════════════════════════════════════════════ */}
          <UiSection id="accordion" title="Accordion"
            subtitle="Collapsible section. Use for FAQs, grouped settings, or advanced options that don't need to be visible by default.">
            <UiPreview fullWidth variants={[
              {
                label: 'Basic',
                preview: (
                  <Box sx={{ width: '100%' }}>
                    {['Advanced filters','Export settings','Danger zone'].map((title, i) => (
                      <Accordion key={i} disableGutters>
                        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                          <Typography fontWeight={600}>{title}</Typography>
                        </AccordionSummary>
                        <AccordionDetails>
                          <Typography variant="body2" color="text.secondary">Content for {title.toLowerCase()}.</Typography>
                        </AccordionDetails>
                      </Accordion>
                    ))}
                  </Box>
                ),
                code: `import { Accordion, AccordionSummary, AccordionDetails } from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

<Accordion disableGutters>
  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
    <Typography fontWeight={600}>Advanced Filters</Typography>
  </AccordionSummary>
  <AccordionDetails>
    <TextField label="Min date" size="small" />
    <TextField label="Max date" size="small" />
  </AccordionDetails>
</Accordion>`,
              },
            ]} />
          </UiSection>

          {/* ══ PROGRESS ════════════════════════════════════════════════ */}
          <UiSection id="progress" title="Progress Indicators"
            subtitle="Show loading state. Use CircularProgress for button/icon loading, LinearProgress for page-level or determinate progress, Skeleton for content placeholders.">
            <UiPreview variants={[
              {
                label: 'Circular',
                preview: <>
                  <CircularProgress size={24} />
                  <CircularProgress size={40} />
                  <CircularProgress size={24} color="success" />
                  <CircularProgress variant="determinate" value={72} size={48} />
                </>,
                code: `// Spinner — use inside buttons or icons
<CircularProgress size={24} />

// Determinate (shows a percentage):
<CircularProgress variant="determinate" value={75} size={48} />

// Inside a button (do this only if not using LoadingButton):
<Button disabled startIcon={<CircularProgress size={16} color="inherit" />}>
  Saving…
</Button>`,
              },
              {
                label: 'Linear',
                preview: (
                  <Stack spacing={2} sx={{ width: '100%' }}>
                    <LinearProgress />
                    <LinearProgress color="success" />
                    <LinearProgress variant="determinate" value={65} />
                    <LinearProgress variant="buffer" value={50} valueBuffer={75} />
                  </Stack>
                ),
                code: `// Indeterminate — for unknown duration
<LinearProgress />

// Determinate — when you know the percentage
<LinearProgress variant="determinate" value={progress} />

// The global loading bar (already in GlobalFeedback):
// It shows automatically via useLoadingBar() when any query is in-flight.
// You don't need to add it manually.`,
              },
            ]} />
          </UiSection>

          {/* ══ SNACKBAR ════════════════════════════════════════════════ */}
          <UiSection id="feedback" title="Snackbar"
            subtitle="Transient feedback message (saved, deleted, error). In this project, always use useAppStore.notify() — never mount Snackbar directly. The global Snackbar is already in GlobalFeedback.">
            <UiPreview variants={[
              {
                label: 'Live demo',
                preview: <>
                  <Button variant="outlined" onClick={() => setSnackOpen(true)}>Show Snackbar</Button>
                  <Snackbar open={snackOpen} autoHideDuration={3000} onClose={() => setSnackOpen(false)} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
                    <Alert severity="success" variant="filled" onClose={() => setSnackOpen(false)}>Changes saved successfully.</Alert>
                  </Snackbar>
                </>,
                code: `// ✅ Always use this — never mount Snackbar manually:
import { useAppStore } from '@/shared/stores/useAppStore';

const { notify } = useAppStore();

notify('User created', 'success');    // green
notify('Network error', 'error');     // red
notify('Please review', 'warning');   // orange
notify('File ready', 'info');         // blue

// For undo-able actions (shows UNDO button for 5s):
notify('User deleted. Click Undo to reverse.', 'info');`,
                do: 'Always use notify() from useAppStore — it goes through GlobalFeedback which already handles positioning, auto-dismiss, and the UNDO button.',
                dont: "Don't import and mount Snackbar/Alert yourself in page components.",
              },
            ]} />
          </UiSection>

          {/* ══ LAYOUT ══════════════════════════════════════════════════ */}
          <UiSection id="layout" title="Grid & Stack"
            subtitle="Two layout primitives. Use Grid for responsive 12-column layouts (page structure, card grids). Use Stack for linear sequences of elements with consistent spacing.">
            <DocCodeBlock language="tsx" code={`import { Grid, Stack, Box } from '@mui/material';

// Grid — responsive columns
<Grid container spacing={3}>
  <Grid size={{ xs: 12, md: 6, lg: 4 }}>     {/* 12 cols on mobile, 6 on tablet, 4 on desktop */}
    <StatCard title="Users" value={1245} />
  </Grid>
  <Grid size={{ xs: 12, md: 6, lg: 4 }}>
    <StatCard title="Revenue" value="$12k" />
  </Grid>
</Grid>

// Stack — linear sequence (horizontal or vertical)
<Stack direction="row" spacing={2} alignItems="center">
  <Avatar>A</Avatar>
  <Typography>Alice Martin</Typography>
  <Chip label="admin" size="small" color="error" />
</Stack>

<Stack spacing={2}>      {/* vertical by default */}
  <TextField label="Name" />
  <TextField label="Email" />
  <Button type="submit">Save</Button>
</Stack>

// Box — generic container (use for sx overrides)
<Box sx={{ p: 3, bgcolor: 'action.hover', borderRadius: 2 }}>
  Content
</Box>`} />
            <Alert severity="info" sx={{ mt: 2 }}>
              <strong>Grid v2 syntax:</strong> This project uses MUI v7 which uses <code>size={`{{ xs: 12, md: 6 }}`}</code> on Grid items — not the old <code>xs={12} md={6}</code> pattern. Always use the new syntax.
            </Alert>
          </UiSection>

          {/* ══ TYPOGRAPHY ══════════════════════════════════════════════ */}
          <UiSection id="typography" title="Typography"
            subtitle="Text rendering with semantic variants. Always use Typography instead of raw <p>, <h1> etc. — it inherits the theme font and colors automatically.">
            <UiPreview fullWidth variants={[
              {
                label: 'Scale',
                preview: (
                  <Stack spacing={0.5} sx={{ width: '100%' }}>
                    {[['h3','Page title (h3 + fontWeight=800)'],['h4','Section heading (h4 + fontWeight=700)'],['h5','Sub-section (h5 + fontWeight=700)'],['h6','Card title'],['subtitle1','Subtitle / label'],['subtitle2','Smaller label'],['body1','Body text — main paragraphs'],['body2','Body 2 — secondary paragraphs'],['caption','Caption — helper text, timestamps']].map(([v, desc]) => (
                      <Stack key={v} direction="row" spacing={3} alignItems="baseline">
                        <Typography variant={v as 'h3'} noWrap sx={[v.startsWith('h') ? { fontWeight: v==='h3'?800:700 } : {}, { minWidth: 160 }]}>{v}</Typography>
                        <Typography variant="caption" color="text.disabled">{desc}</Typography>
                      </Stack>
                    ))}
                  </Stack>
                ),
                code: `<Typography variant="h3" fontWeight={800}>Page Title</Typography>
<Typography variant="h4" fontWeight={700}>Section Heading</Typography>
<Typography variant="h5" fontWeight={700}>Sub-section</Typography>
<Typography variant="subtitle1" fontWeight={600}>Card label</Typography>
<Typography variant="body1">Main paragraph text</Typography>
<Typography variant="body2" color="text.secondary">Secondary text</Typography>
<Typography variant="caption" color="text.secondary">Timestamp, helper</Typography>

// Color shortcuts:
<Typography color="text.primary">Default text</Typography>
<Typography color="text.secondary">Dimmed text</Typography>
<Typography color="text.disabled">Placeholder / hint</Typography>
<Typography color="primary.main">Accent</Typography>
<Typography color="error.main">Error</Typography>
<Typography color="success.main">Success</Typography>`,
              },
            ]} />
          </UiSection>

        </Grid>
      </Grid>
    </Box>
  );
}
