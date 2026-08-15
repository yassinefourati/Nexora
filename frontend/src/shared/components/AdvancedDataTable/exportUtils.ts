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
export async function exportPdf<T extends object>(rows: T[], columns: ColumnDef<T>[], title = 'Export', filename = 'export') {
  const { jsPDF } = await import('jspdf');
  const { default: autoTable } = await import('jspdf-autotable');
  const cols = columns.filter((c) => c.field !== ('actions' as keyof T));
  const doc = new jsPDF({ orientation: 'landscape' });
  doc.setFontSize(14); doc.text(title, 14, 16);
  autoTable(doc, { startY: 24, head: [cols.map((c) => c.headerName)], body: rows.map((row) => cols.map((col) => col.exportValue ? col.exportValue(row) : String(row[col.field as keyof T] ?? ''))), styles: { fontSize: 9 }, headStyles: { fillColor: [25, 118, 210] } });
  doc.save(`${filename}.pdf`);
}
