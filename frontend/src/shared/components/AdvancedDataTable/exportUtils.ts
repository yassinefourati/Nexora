import type { ColumnDef } from './types';
export function exportCsv<T extends object>(rows: T[], columns: ColumnDef<T>[], filename = 'export') {
  const cols = columns.filter((c) => c.field !== ('actions' as keyof T));
  const header = cols.map((c) => `"${c.headerName}"`).join(',');
  const body = rows.map((row) => cols.map((col) => { const raw = col.exportValue ? col.exportValue(row) : String(row[col.field as keyof T] ?? ''); return `"${raw.replace(/"/g, '""')}"`; }).join(',')).join('\n');
  const blob = new Blob([`${header}\n${body}`], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a'); a.href = url; a.download = `${filename}.csv`; a.click();
  URL.revokeObjectURL(url);
}
