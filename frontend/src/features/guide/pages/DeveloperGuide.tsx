import { Box, Typography, Chip, Stack, Divider, Tab, Tabs, Alert, IconButton, Tooltip } from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import CheckIcon from '@mui/icons-material/Check';
import { useState } from 'react';
import { useAppStore } from '@/shared/stores/useAppStore';
import AdvancedDataTable from '@/shared/components/AdvancedDataTable/AdvancedDataTable';
import type { ColumnDef } from '@/shared/components/AdvancedDataTable/types';

interface SampleRow { id: number; name: string; email: string; role: string; status: 'active' | 'inactive'; joined: string; }
const SAMPLE_ROWS: SampleRow[] = Array.from({ length: 28 }, (_, i) => ({
  id: i+1, name: ['Alice Martin','Bob Tremblay','Claire Dubois','David Lavoie','Eva Bergeron'][i%5],
  email: `user${i+1}@demo.com`, role: ['admin','editor','viewer'][i%3],
  status: i % 4 === 0 ? 'inactive' : 'active',
  joined: new Date(2023, i%12, (i%28)+1).toLocaleDateString(),
}));
const SAMPLE_COLUMNS: ColumnDef<SampleRow>[] = [
  { field:'id', headerName:'ID', width:60 },
  { field:'name', headerName:'Name', flex:1 },
  { field:'email', headerName:'Email', flex:1.2 },
  { field:'role', headerName:'Role', width:100, renderCell:(row) => <Chip label={row.role} size="small" color={row.role==='admin'?'error':row.role==='editor'?'warning':'default'} variant="outlined" />, exportValue:(row)=>row.role },
  { field:'status', headerName:'Status', width:100, renderCell:(row) => <Chip label={row.status} size="small" color={row.status==='active'?'success':'default'} />, exportValue:(row)=>row.status },
  { field:'joined', headerName:'Joined', width:120 },
];

function CodeBlock({ code }: { code: string }) {
  const [copied, setCopied] = useState(false); const { notify } = useAppStore();
  return (
    <Box sx={{ position:'relative', my:1.5 }}>
      <Tooltip title={copied ? 'Copied!' : 'Copy'}>
        <IconButton size="small" onClick={() => { navigator.clipboard.writeText(code); setCopied(true); notify('Copied!','success'); setTimeout(()=>setCopied(false),2000); }} sx={{ position:'absolute', top:8, right:8, color:'grey.400','&:hover':{color:'white'} }}>
          {copied ? <CheckIcon fontSize="small" /> : <ContentCopyIcon fontSize="small" />}
        </IconButton>
      </Tooltip>
      <Box component="pre" sx={{ bgcolor:'#1e1e1e', color:'#d4d4d4', p:2, pr:5, borderRadius:2, overflowX:'auto', fontSize:'0.8rem', lineHeight:1.6, m:0, fontFamily:'Consolas,"Courier New",monospace' }}><code>{code}</code></Box>
    </Box>
  );
}
function Section({ step, title, children }: { step: number; title: string; children: React.ReactNode }) {
  return (
    <Box sx={{ mb:4 }}>
      <Stack direction="row" spacing={1.5} alignItems="center" mb={1.5}>
        <Box sx={{ width:28, height:28, borderRadius:'50%', bgcolor:'primary.main', color:'white', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'0.8rem', fontWeight:700, flexShrink:0 }}>{step}</Box>
        <Typography variant="h6" fontWeight={700}>{title}</Typography>
      </Stack>
      {children}
    </Box>
  );
}

const SNIPPETS = {
  api: `// src/features/products/api/productsApi.ts
import apiClient from '@/core/api/client';
export interface Product { id: number; name: string; price: number; }
export const getProducts = () => apiClient.get<Product[]>('/products').then(r => r.data);
export const createProduct = (body: Omit<Product,'id'>) => apiClient.post<Product>('/products', body).then(r => r.data);
export const updateProduct = (id: number, body: Partial<Product>) => apiClient.put<Product>(\`/products/\${id}\`, body).then(r => r.data);
export const deleteProduct = (id: number) => apiClient.delete(\`/products/\${id}\`).then(r => r.data);`,
  schema: `// src/features/products/schemas/productSchema.ts
import { z } from 'zod';
export const productSchema = z.object({
  name: z.string().min(2, 'At least 2 characters'),
  price: z.number().positive(),
});
export type ProductFormData = z.infer<typeof productSchema>;`,
  hooks: `// src/features/products/hooks/useProducts.ts
import { useQuery, useMutation } from '@tanstack/react-query';
import { getProducts, deleteProduct } from '../api/productsApi';
import { queryClient } from '@/shared/lib/queryClient';
import { useAppStore } from '@/shared/stores/useAppStore';
export function useProducts() { return useQuery({ queryKey: ['products'], queryFn: getProducts }); }
export function useDeleteProduct() {
  const { notify } = useAppStore();
  return useMutation({ mutationFn: deleteProduct,
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['products'] }); notify('Deleted','success'); }
  });
}`,
  mock: `// src/mocks/handlers/productsHandlers.ts
import { http, HttpResponse } from 'msw';
const db = [{ id:1, name:'Widget A', price:29.99 }];
export const productsHandlers = [
  http.get('/api/products', () => HttpResponse.json(db)),
  http.delete('/api/products/:id', ({ params }) => {
    const idx = db.findIndex(p => p.id === Number(params.id));
    db.splice(idx, 1);
    return HttpResponse.json({ success: true });
  }),
];`,
  route: `// 1. src/core/router/routes.ts
PRODUCTS: '/products',

// 2. src/layout/config/menuConfig.tsx
{ label: 'Products', icon: <InventoryIcon />, path: ROUTES.PRODUCTS },

// 3. src/core/router/AppRoutes.tsx
<Route path={ROUTES.PRODUCTS} element={<Protected><Products /></Protected>} />

// 4. src/mocks/browser.ts — register the handler
import { productsHandlers } from './handlers/productsHandlers';
export const worker = setupWorker(...productsHandlers, ...otherHandlers);`,
};

const TABS = ['Overview','Live Demo','1. API','2. Schema','3. Hooks','4. MSW Mock','5. Register'];
export default function DeveloperGuide() {
  const [tab, setTab] = useState(0);
  return (
    <Box>
      <Stack direction="row" spacing={2} alignItems="center" mb={1}><Typography variant="h4" fontWeight={700}>Developer Guide</Typography><Chip label="CRUD pattern" color="primary" size="small" /></Stack>
      <Typography variant="body2" color="text.secondary" mb={3}>Step-by-step guide to add any new CRUD feature. Follows the same pattern as the Users module.</Typography>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" scrollButtons="auto" sx={{ mb:3, borderBottom:1, borderColor:'divider' }}>
        {TABS.map((t) => <Tab key={t} label={t} />)}
      </Tabs>
      {tab === 0 && <Box>
        <Alert severity="info" sx={{ mb:3 }}>Every feature follows the same 5-file pattern. Copy, rename, and you have a working CRUD page.</Alert>
        <Section step={1} title="Create the API layer"><Typography variant="body2" color="text.secondary"><code>src/features/[name]/api/[name]Api.ts</code> — one function per HTTP verb: getAll, create, update, delete.</Typography></Section>
        <Section step={2} title="Define the Zod schema"><Typography variant="body2" color="text.secondary"><code>src/features/[name]/schemas/[name]Schema.ts</code> — validates form data. Type inferred with <code>z.infer</code>.</Typography></Section>
        <Section step={3} title="Write React Query hooks"><Typography variant="body2" color="text.secondary"><code>src/features/[name]/hooks/use[Name].ts</code> — useQuery for reads, useMutation for writes. Invalidates cache on success.</Typography></Section>
        <Section step={4} title="Add an MSW mock handler"><Typography variant="body2" color="text.secondary"><code>src/mocks/handlers/[name]Handlers.ts</code> — intercepts HTTP calls in dev. Register in <code>src/mocks/browser.ts</code>.</Typography></Section>
        <Section step={5} title="Build the page"><Typography variant="body2" color="text.secondary">Use <code>AdvancedDataTable</code> for sort, filter, CSV/PDF export. Wrap actions with <code>&lt;Can&gt;</code> for RBAC.</Typography></Section>
        <Divider sx={{ my:3 }} />
        <Section step={6} title="Register route + menu"><Typography variant="body2" color="text.secondary" mb={1}>Add to routes.ts, menuConfig.tsx, AppRoutes.tsx, and mocks/browser.ts:</Typography><CodeBlock code={SNIPPETS.route} /></Section>
      </Box>}
      {tab === 1 && <Box><Alert severity="success" sx={{ mb:3 }}>Live <strong>AdvancedDataTable</strong> demo — sort, search, filter, then export to CSV or PDF.</Alert><AdvancedDataTable rows={SAMPLE_ROWS} columns={SAMPLE_COLUMNS} title="Sample Users" exportFilename="sample-users" /></Box>}
      {tab === 2 && <><Alert severity="info" sx={{ mb:2 }}>File: <code>src/features/products/api/productsApi.ts</code></Alert><CodeBlock code={SNIPPETS.api} /></>}
      {tab === 3 && <><Alert severity="info" sx={{ mb:2 }}>File: <code>src/features/products/schemas/productSchema.ts</code></Alert><CodeBlock code={SNIPPETS.schema} /></>}
      {tab === 4 && <><Alert severity="info" sx={{ mb:2 }}>File: <code>src/features/products/hooks/useProducts.ts</code></Alert><CodeBlock code={SNIPPETS.hooks} /></>}
      {tab === 5 && <><Alert severity="info" sx={{ mb:2 }}>File: <code>src/mocks/handlers/productsHandlers.ts</code></Alert><CodeBlock code={SNIPPETS.mock} /></>}
      {tab === 6 && <><Alert severity="info" sx={{ mb:2 }}>Register in 4 files:</Alert><CodeBlock code={SNIPPETS.route} /></>}
    </Box>
  );
}
