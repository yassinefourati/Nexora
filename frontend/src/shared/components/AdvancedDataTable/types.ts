import type { ReactNode } from 'react';
export interface ColumnDef<T> { field: keyof T | 'actions'; headerName: string; width?: number; flex?: number; sortable?: boolean; filterable?: boolean; renderCell?: (row: T) => ReactNode; exportValue?: (row: T) => string; }
export interface FilterState { field: string; operator: 'contains' | 'equals' | 'startsWith'; value: string; }
export interface SortState { field: string; direction: 'asc' | 'desc'; }
